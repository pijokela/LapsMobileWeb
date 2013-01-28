package my.laps.mobileweb

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import my.laps.mobile.LapValidator
import my.laps.mobile.PracticeWebsiteDao
import my.laps.mobile.datastore.PracticeDatastoreDao

class RssServlet extends HttpServlet with HttpServletRequestParsing {

  val websiteDao = new PracticeWebsiteDao("http://www.mylaps.com", new MylapsConf())
  val dao = new PracticeDatastoreDao(websiteDao)

  override def doGet(req : HttpServletRequest, resp : HttpServletResponse) = {
    val validator = LapValidator.createFromParam(req)
    val conf = UserConf.parseFromParams(req)
	val rss = new RssXml(conf)
    
	resp.setContentType("text/xml")
	val out = resp.getWriter()
		
	val Some(tid) = longParamOption("tid", req)
	val day = dao.getTrackPracticeDay(tid);
	out.println(rss.trackActivityRss(day).toString);
  }
}