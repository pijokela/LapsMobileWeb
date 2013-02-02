package my.laps.mobile

import scala.io.Source
import my.laps.mobileweb.MylapsConf
import org.junit.Test
import org.junit.Assert
import my.laps.mobile.practice1.PracticeSessionParser
import my.laps.mobile.practice1.DriverNameParser


class Utf8DriverName {
  val so = Source.fromFile("test/my/laps/mobile/utf8-driver-name.txt")
  val parser = new DriverNameParser()
  
  @Test
  def driverFoundFromLine() {
    val line = """	<h1><img src="http://www.mylaps.com/textimage/headerDark/h1/Laptimes%2Bfor%2BPetri%2BNiemel%25C3%25A4.gif" alt="Laptimes for Petri Niemelä"/><span class="hide">Laptimes for Petri Niemelä</span></h1>"""
    val m = parser.parse(line)
    Assert.assertTrue(m != None)
    Assert.assertEquals("Petri Niemelä", m.get)
  }
  
  @Test
  def driverFoundFromLineWithDots() {
    val line = """	<h1><img src="http://www.mylaps.com/textimage/headerDark/h1/Laptimes%2Bfor%2BPetri%2BNiemel%25C3%25A4.gif" alt="Laptimes for Petri Niemelä"/><span class="hide">Laptimes for N.O.X.</span></h1>"""
    val m = parser.parse(line)
    Assert.assertTrue(m != None)
    Assert.assertEquals("N.O.X.", m.get)
  }
  
  @Test
  def driverFound() {
    val track = TrackStatus(true, "name", "location", 1429, Length(70, "m"))
    val parser = new PracticeSessionParser(track, so, new LapValidator(1000, 100000), new MylapsConf())
    val r = parser.parseTrackPracticeDay
    Assert.assertNotNull(r)
    println(r)
  }
}

/**
 * http://www.mylaps.com/practice/showLaptimes.jsp?tid=1429&transponder=5778155
 */
object TestPracticeSessionParser {
  def main(args: Array[String]) {
    val so = Source.fromFile("test/my/laps/mobile/practice-session-data.txt")
    
    val track = TrackStatus(true, "name", "location", 1429, Length(70, "m"))
    val parser = new PracticeSessionParser(track, so, new LapValidator(1000, 100000), new MylapsConf())
    val r = parser.parseTrackPracticeDay
//    Assert.assertNotNull(r)
    println(r)
  }
  
//  def main(args : Array[String]) {
//    val so = Source.fromURL("http://www.mylaps.com/practice/showLaptimes.jsp?tid=1429&transponder=5778155")
//    println(so.getLines.mkString("\n"))
//  }
  
}