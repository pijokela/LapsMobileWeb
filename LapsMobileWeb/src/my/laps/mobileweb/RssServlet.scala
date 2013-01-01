package my.laps.mobileweb

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import my.laps.mobile.LapValidator
import my.laps.mobile.PracticeWebsiteDao

class RssServlet extends HttpServlet with HttpServletRequestParsing {
  
  override def doGet(req : HttpServletRequest, resp : HttpServletResponse) = {
    val validator = LapValidator.createFromParam(req)
    val conf = UserConf.parseFromParams(req)
	val rss = new RssXml(conf)
	val dao = new PracticeWebsiteDao("http://www.mylaps.com", new MylapsConf())
    
	resp.setContentType("text/xml")
	val out = resp.getWriter()
		
	val Some(tid) = longParamOption("tid", req)
	val day = dao.getTrackPracticeDay(tid);
	out.println(rss.trackActivityRss(day).toString);
  }
}