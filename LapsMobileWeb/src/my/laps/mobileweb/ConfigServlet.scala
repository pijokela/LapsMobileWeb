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
import my.laps.mobileweb.HttpServletRequestParsing.toMyRequest
import my.laps.mobileweb.HttpServletRequestParsing.toMyResponse
import my.laps.mobile.practice1.AllLapsFromUserOnTrackDao
import my.laps.mobile.datastore.TrackStatusDao

class ConfigServlet extends HttpServlet with HttpServletRequestParsing {
  
  
  val mylapsConf = new MylapsConf()
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
      case e : java.lang.IllegalArgumentException => 
        val out = resp.getWriter()
        val html = new Html(UserConf.createWithDefaults(timeService)) // Create default user so that creation will not fail.
        out.println(html.header)
        out.println(html.illegalArgumentPage)
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
		html.showConfigOptionsPage(validator, conf)
      }
    }
    
	val out = resp.getWriter()
	out.println(html.header)
	out.println(pageContent)
	out.println(html.footer)
  }
}

