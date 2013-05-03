package my.laps.mobile

import scala.io.Source
import my.laps.mobileweb._
import org.junit.Test
import org.junit.Assert
import java.util.Calendar
import my.laps.mobile.practice1.TrackStatusParser

class Tamua20130503 {
  val so = Source.fromFile("test/my/laps/mobile/tamua20130503.txt")
  val user = UserConf.createWithDefaults(TestTimeService("2013-05-03T10:10:10.EET"))
  
  def parse() = {
    val parser = new TrackStatusParser(so, new MylapsConf())
    println(parser.tid)
    println(parser.trackName)
    parser.parseTrackPracticeDay
  }
  
  @Test
  def has10driversTotal() {
    val r = parse()
    Assert.assertEquals(50, r.practiceSessions.size)
  }

  @Test
  def has1driverToday() {
    val r = parse()
    val sessions = r.practiceSessions.filter(s=>user.isToday(s.date))
    println(sessions.size)
    Assert.assertEquals(1, sessions.size)
  }

  @Test
  def pirkkasTimeIs2100() {
    val r = parse()
    val s = r.sessionsNewestFirst.find(_.driver.name == "Pirkka").get
    Assert.assertEquals("21:00:00", user.formatTime(s.date))
    val c = Calendar.getInstance()
    c.setTimeZone(UserConf.TimeZoneFI)
    c.setTime(s.date)
    Assert.assertEquals(21, c.get(Calendar.HOUR_OF_DAY))
  }

  @Test
  def trackDataIsCorrect() {
    val r = parse()
    val t = r.track
    Assert.assertEquals("TamUA:n mattorata", t.name)
    Assert.assertEquals(Length(0.07, "km"), t.length)
    Assert.assertEquals("Nokia", t.location)
    Assert.assertEquals(true, t.online)
  }

  @Test
  def tidIs1429() {
    val r = parse()
    val t = r.track.tid
    Assert.assertEquals(1429, t)
  }

}

object TestTrackParser {

  def main(args : Array[String]) {
    val so = Source.fromFile("test/my/laps/mobile/track-data-prca.txt")
    
    val parser = new TrackStatusParser(so, new MylapsConf())
    println(parser.tid)
    println(parser.trackName)
    val r = parser.parseTrackPracticeDay
//    Assert.assertNotNull(r)
    println(r)
  }
  
//  def main(args : Array[String]) {
//    val so = Source.fromURL("http://www.mylaps.com/practice/showTrack.jsp?tid=1429")
//    println(so.getLines.mkString("\n"))
//  }
  
}