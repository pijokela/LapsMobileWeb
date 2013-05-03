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
import my.laps.mobile.practice1.AllLapsFromUserOnTrackDao
import my.laps.mobile.datastore.TrackStatusDao


class RssServlet extends HttpServlet with HttpServletRequestParsing {

  val mylapsConf = new MylapsConf()
  val webDao = new PracticeWebsiteDao("http://practice.mylaps.com", mylapsConf)
  val lapsFromUserDao = new AllLapsFromUserOnTrackDao("http://practice.mylaps.com", mylapsConf)
  val trackStatusDao = new TrackStatusDao(webDao)
  val dao = new PracticeDatastoreDao(webDao, trackStatusDao, lapsFromUserDao)
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