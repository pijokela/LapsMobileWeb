package my.laps.mobileweb

import javax.servlet.http.HttpServlet
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Cookie
import my.laps.mobile.LapValidator
import my.laps.mobile.Html
import java.util.Locale
import my.laps.mobile.PracticeWebsiteDao
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

class LapsMobileServlet extends HttpServlet with HttpServletRequestParsing {
  
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
    val validator = getValidator(req)
    val conf = UserConf.parseFromRequest(req)
	val html = new Html(conf)
	val dao = new PracticeWebsiteDao("http://www.mylaps.com", new MylapsConf())
    
	resp.setContentType("text/html")
	val out = resp.getWriter()
		
	out.println(html.header);
    
	val trackId = longParamOption("tid", req)
    val tp = longParamOption("transponder", req)
    (trackId, tp) match {
      case (None, None) => {
		val tid = getTidFromCookie(req)
		// Output track selection page:
		out.println(html.selectTrackPage(tid, validator, conf))
      }
      case (Some(tid), None) => {
		putTidToCookie(tid, resp);
		// Output track practice day:
		val day = dao.getTrackPracticeDay(tid);
		out.println(html.trackTrainingDay(day));
      }
      case (Some(tid), Some(transponder)) => {
		putTidToCookie(tid, resp);
		// Output transponder session list:
		val day = dao.getTransponderSessions(tid, transponder, validator);
		out.println(html.transponderSessions(day));
      }
    }
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
  
  def cookieOption(name : String, req : HttpServletRequest) : Option[Cookie] = {
    val cookies = req.getCookies()
    if (cookies == null) return None
    cookies.find(_.getName() == name)
  }
  
  def cookieValueOption(name : String, req : HttpServletRequest) : Option[String] = 
    cookieOption(name, req).map(_.getValue)
  
  def getTidFromCookie(req : HttpServletRequest) =
    cookieOption("tid", req).map(_.getValue().toLong).getOrElse(1429L)

  def putTidToCookie(tid : Long, resp : HttpServletResponse) = 
	resp.addCookie(new Cookie("tid", tid.toString))
	
  def putCookie(name : String, value : String, resp : HttpServletResponse) = 
	resp.addCookie(new Cookie(name, value))
}