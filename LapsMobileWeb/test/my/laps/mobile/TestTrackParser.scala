package my.laps.mobile

import scala.io.Source
import my.laps.mobileweb.MylapsConf
import my.laps.mobileweb.UserConf
import org.junit.Test
import org.junit.Assert
import java.util.Calendar

class Tamua20130112 {
  val so = Source.fromFile("test/my/laps/mobile/tamua20130112.txt")
  val user = UserConf.createWithDefaults()
  
  def parse() = {
    val parser = new TrackStatusParser(so, new MylapsConf())
    println(parser.tid)
    println(parser.trackName)
    parser.parseTrackPracticeDay
  }
  
  @Test
  def has10driversTotal() {
    val r = parse()
    Assert.assertEquals(10, r.practiceSessions.size)
  }

  @Test
  def has4driversToday() {
    val r = parse()
    Assert.assertEquals(4, 
        r.practiceSessions.filter(s=>user.isToday(s.date)).size)
  }

  @Test
  def pirkkasTimeIs1731() {
    val r = parse()
    val s = r.sessionsNewestFirst.find(_.driver.name == "Pirkka").get
    Assert.assertEquals("17:31:00", user.formatTime(s.date))
    val c = Calendar.getInstance()
    c.setTimeZone(UserConf.TimeZoneFI)
    c.setTime(s.date)
    Assert.assertEquals(17, c.get(Calendar.HOUR_OF_DAY))
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