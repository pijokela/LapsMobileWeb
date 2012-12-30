package my.laps.mobile

//import org.junit.Test
import scala.io.Source
//import org.junit.Assert

object TestTrackParser {

  def main(args : Array[String]) {
    val so = Source.fromFile("test/my/laps/mobile/track-data-prca.txt")
    
    val parser = new TrackStatusParser(so)
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