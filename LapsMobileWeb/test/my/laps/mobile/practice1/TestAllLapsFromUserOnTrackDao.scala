package my.laps.mobile.practice1

import my.laps.mobileweb.UserConf
import scala.io.Source
import my.laps.mobile.TestTimeService
import my.laps.mobileweb.MylapsConf
import org.junit.Assert
import org.junit.Test

class TestAllLapsFromUserOnTrackDao {
  val so = Source.fromFile("test/my/laps/mobile/practice1/AllLaptimesTransponder5916939.txt")
  val user = UserConf.createWithDefaults(TestTimeService("2013-01-12T10:10:10.EET"))
  
  @Test
  def testParsingFlatTextFile() {
    val parser = new AllPracticeSessionsParser(so, new MylapsConf)
    Assert.assertFalse(parser.parsePracticeSessions.isEmpty)
  }
}