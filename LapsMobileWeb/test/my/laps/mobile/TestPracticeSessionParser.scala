package my.laps.mobile

//import org.junit.Test
//import org.junit.Assert
import scala.io.Source

/**
 * http://www.mylaps.com/practice/showLaptimes.jsp?tid=1429&transponder=5778155
 */
object TestPracticeSessionParser {
  def main(args: Array[String]) {
    val so = Source.fromFile("test/my/laps/mobile/practice-session-data.txt")
    
    val track = TrackStatus(true, "name", "location", 1429, Length(70, "m"))
    val parser = new PracticeSessionParser(track, so, new LapValidator(1000, 100000))
    val r = parser.parseTrackPracticeDay
//    Assert.assertNotNull(r)
    println(r)
  }
  
//  def main(args : Array[String]) {
//    val so = Source.fromURL("http://www.mylaps.com/practice/showLaptimes.jsp?tid=1429&transponder=5778155")
//    println(so.getLines.mkString("\n"))
//  }
  
}