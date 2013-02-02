package my.laps.mobileweb

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import my.laps.mobile.LapValidator
import my.laps.mobile.practice1.PracticeWebsiteDao
import my.laps.mobile.datastore.PracticeDatastoreDao
import my.laps.mobile.Day
import my.laps.mobile.RealTimeService

class LapsMobileServlet extends HttpServlet with HttpServletRequestParsing {
  
  
  val webDao = new PracticeWebsiteDao("http://www.mylaps.com", new MylapsConf())
  val dao = new PracticeDatastoreDao(webDao)
  val timeService = new RealTimeService
  
  /**
   * Configuration change requests are POSTs.
   */
  override def doPost(req : HttpServletRequest, resp : HttpServletResponse) {
    val minMs = longParamOption("minMs", req).get
    val maxMs = longParamOption("maxMs", req).get
	
	val validationCookie = new Cookie("minMs-maxMs", minMs.toString + "-" + maxMs.toString)
	resp.addCookie(validationCookie)
	
	paramOption("lapTimeFormat", req).foreach(s=>resp.addCookie(new Cookie("lapTimeFormat", s)))
	paramOption("dateFormat", req).foreach(s=>resp.addCookie(new Cookie("dateFormat", s)))
	paramOption("timeFormat", req).foreach(s=>resp.addCookie(new Cookie("timeFormat", s)))
	
	resp.sendRedirect("?")
  }
  
  private def getValidator(req : HttpServletRequest) : LapValidator = 
    cookieOption("minMs-maxMs", req).map(
      cookie=>{
        val parts = cookie.getValue().split("-")
		val minMs = parts(0).toLong
		val maxMs = parts(1).toLong
		new LapValidator(minMs, maxMs)
      }).getOrElse(new LapValidator(5000, 25000))

  override def doGet(req : HttpServletRequest, resp : HttpServletResponse) = {
    try {
      doGetWithErrorHandling(req, resp)
    }
    catch {
      case e : java.net.SocketTimeoutException => 
        val out = resp.getWriter()
        val html = new Html(UserConf.createWithDefaults(timeService)) // Create default user so that creation will not fail.
        out.println(html.header)
        out.println(html.errorPage)
        out.println(html.footer)
    }
  }
  
  def doGetWithErrorHandling(req : HttpServletRequest, resp : HttpServletResponse) = {
    val validator = LapValidator.createFromCookie(req)
    val conf = UserConf.parseFromCookies(req, timeService)
	val html = new Html(conf)
    
	resp.setContentType("text/html")
    
	val trackId = longParamOption("tid", req)
    val tp = longParamOption("transponder", req)
    val day = dayParamOption("day", req)
    
    val pageContent = (trackId, tp) match {
      case (None, None) => {
		val tid = getTidFromCookie(req)
		// Output track selection page:
		html.selectTrackPage(tid, validator, conf)
      }
      case (Some(tid), None) => {
		putTidToCookie(tid, resp)
		// Output track practice day:
		val trackDay = dao.getTrackPracticeDay(tid)
		html.trackTrainingDay(trackDay)
      }
      case (Some(tid), Some(transponder)) => {
		putTidToCookie(tid, resp)
		// Output transponder session list:
		val practiceDay = dao.getTransponderSessions(tid, transponder, day, validator)
		html.transponderSessions(practiceDay)
      }
    }
    
	val out = resp.getWriter()
	out.println(html.header)
	out.println(pageContent)
	out.println(html.footer)
  }
}

trait HttpServletRequestParsing {
  def paramOption(name : String, req : HttpServletRequest) : Option[String] = 
    if (req.getParameter(name) == null) None else Some(req.getParameter(name))
  
  def longParamOption(name : String, req : HttpServletRequest) : Option[Long] = 
    try {
      paramOption(name, req).map(_.toLong)
    }
    catch {
      case t : NumberFormatException => None
    }
    
  def dayParamOption(name : String, req : HttpServletRequest) : Option[Day] = 
    try {
      val dayString = paramOption(name, req).getOrElse(return None)
      val parts = dayString.split("-").map(_.toInt)
      Some(Day(parts(0), parts(1), parts(2)))
    }
    catch {
      case t : NumberFormatException => None
      case t : ArrayIndexOutOfBoundsException => None
    }
    
  
  def cookieOption(name : String, req : HttpServletRequest) : Option[Cookie] = {
    val cookies = req.getCookies()
    if (cookies == null) return None
    cookies.find(_.getName() == name)
  }
  
  def cookieValueOption(name : String, req : HttpServletRequest) : Option[String] = 
    cookieOption(name, req).map(_.getValue)
  
  def paramValueOption(name : String, req : HttpServletRequest) : Option[String] =
    req.getParameter(name) match {
      case null => None
      case v => Some(v)
    }
  
  def getTidFromCookie(req : HttpServletRequest) =
    cookieOption("tid", req).map(_.getValue().toLong).getOrElse(1429L)

  def putTidToCookie(tid : Long, resp : HttpServletResponse) = 
	resp.addCookie(new Cookie("tid", tid.toString))
	
  def putCookie(name : String, value : String, resp : HttpServletResponse) = {
    val cookie = new Cookie(name, value)
    cookie.setMaxAge(3600*24*365)
    resp.addCookie(cookie)
  }
}