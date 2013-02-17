package my.laps.mobileweb

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import my.laps.mobile.LapValidator
import my.laps.mobile.practice1.PracticeWebsiteDao
import my.laps.mobile.datastore.PracticeDatastoreDao
import my.laps.mobile.RealTimeService
import my.laps.mobileweb.HttpServletRequestParsing.toMyRequest
import my.laps.mobileweb.HttpServletRequestParsing.toMyResponse


class RssServlet extends HttpServlet with HttpServletRequestParsing {

  val websiteDao = new PracticeWebsiteDao("http://www.mylaps.com", new MylapsConf())
  val dao = new PracticeDatastoreDao(websiteDao)
  val timeService = new RealTimeService

  override def doGet(req : HttpServletRequest, resp : HttpServletResponse) = {
    val validator = LapValidator.createFromParam(req)
    val conf = UserConf.parseFromParams(req, timeService)
	val rss = new RssXml(conf)
    
	resp.setContentType("text/xml")
	val out = resp.getWriter()
		
	val Some(tid) = longParamOption("tid", req)
	val day = dao.getTrackPracticeDay(tid);
	out.println(rss.trackActivityRss(day).toString);
  }
}