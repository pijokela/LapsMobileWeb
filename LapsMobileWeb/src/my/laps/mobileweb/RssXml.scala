package my.laps.mobileweb

import scala.xml.Elem
import my.laps.mobile.TrackStatus
import my.laps.mobile.TrackPracticeDay
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import java.text.SimpleDateFormat
import java.util.Locale
import java.text.DateFormatSymbols

class RssXml(val conf : UserConf) {
  def trackActivityRss(day : TrackPracticeDay) : Elem = {
    
    // Sat, 5 Jan 2013 14:12:41 +0200
    val pubDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", UserConf.FI)
    pubDateFormat.setDateFormatSymbols(DateFormatSymbols.getInstance(Locale.US))
    
    val track = day.track
    val items = for(session <- day.sessionsNewestFirst.take(10)) yield {
        <item>
          <title>{conf.formatDate(session.date) + " " + session.driver.name + " (" + session.driver.transponder.number + ")"}</title>
          <link>http://m-laps.appspot.com/app?tid={track.tid}&amp;transponder={session.driver.transponder.number}</link>
          <description>
            {session.driver.name}
            Practice session at {track.name} during {conf.formatDate(session.date)}. Total of {session.passings} passings.
          </description>
          <pubDate>{pubDateFormat.format(session.date)}</pubDate>
          <guid isPermaLink="false">trackActivityRss_{session.date.getTime}_{session.driver.transponder.number}</guid>
        </item>
    }
    
    val atomLink = <atom:link href="http://m-laps.appspot.com/rss?tid={track.tid}" rel="self" type="application/rss+xml" /> % 
      Attribute(None, "href", Text("http://m-laps.appspot.com/rss?tid=" + track.tid), Null)
    
    return <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
      <channel>
        <title>{track.name} Practice Sessions</title>
        <link>http://m-laps.appspot.com/app?tid={track.tid}</link>
        <description>This feed lists all practice sessions from {track.name} at 
        {track.location}. Please see website for more details about the sessions.
        </description>
        {atomLink}
        {items}
      </channel>
    </rss> 
  }
}