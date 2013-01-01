package my.laps.mobileweb

import scala.xml.Elem
import my.laps.mobile.TrackStatus
import my.laps.mobile.TrackPracticeDay

class RssXml(val conf : UserConf) {
  def trackActivityRss(day : TrackPracticeDay) : Elem = {
    val track = day.track
    val items = for(session <- day.practiceSessions) yield {
        <item>
          <title>{conf.formatDate(session.date) + " " + session.driver.name + " (" + session.driver.transponder.number + ")"}</title>
          <link>http://m-laps.appspot.com/app?tid={track.tid}&amp;transponder={session.driver.transponder.number}</link>
          <description>
            <h2>{session.driver.name}</h2>
            <p>Practice session at {track.name} during {conf.formatDate(session.date)}. Total of {session.passings} passings.</p>
          </description>
        </item>
    }
    
    <rss version="2.0">
      <channel>
        <title>{track.name} Practice Sessions</title>
        <link>http://m-laps.appspot.com/app?tid={track.tid}</link>
        <description>This feed lists all practice sessions from {track.name} at 
        {track.location}. Please see website for more details about the sessions.
        </description>
        {items}
      </channel>
    </rss> 
  }
}