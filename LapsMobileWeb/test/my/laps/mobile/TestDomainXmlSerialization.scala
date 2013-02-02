package my.laps.mobile

import org.junit._
import java.util.Date

class TestDomainXmlSerialization {
  val transponder = <transponder><number>1234</number></transponder>
  
  @Test
  def transponderRoundTrip() = {
    Assert.assertEquals(transponder.toString, Transponder(transponder).toXml.toString)
  }
  
  val driver = Driver(Transponder(transponder), "The Driver")
  
  @Test
  def driverRoundTrip() = {
    val xml = driver.toXml
    Assert.assertEquals(xml.toString, Driver(xml).toXml.toString)
  }
  
  val day = Day(2013,2,1)
  val timeService = TestTimeService("2013-02-01T10:10:10.EET")
  
  @Test
  def dayRoundTrip() = {
    val xml = day.toXml
    Assert.assertEquals(xml.toString, Day(xml).toXml.toString)
  }
  
  val lap1 = Lap(10200)
  val lap2 = Lap(9100)
  val laps = lap1 :: lap2 :: Nil
  
  @Test
  def lapRoundTrip() = {
    val xml = lap1.toXml
    Assert.assertEquals(xml.toString, Lap(xml).toXml.toString)
  }
  
  val validator = new LapValidator(5000, 15000)
  
  val practiseSession1 = PracticeSession(new Date(timeService.now.getTime()-100000), laps, validator)
  val practiseSession2 = PracticeSession(timeService.now, laps, validator)
  val sessions = practiseSession1 :: practiseSession2 :: Nil

  @Test
  def sessionRoundTrip() = {
    val xml = practiseSession1.toXml
    Assert.assertEquals(xml.toString, PracticeSession(xml).toXml.toString)
  }
  
  val track = TrackStatus(true, "Tamuan mattorata", "Nokia", 1000, Length(70, "m"))
  
  @Test
  def trackRoundTrip() = {
    val xml = track.toXml
    Assert.assertEquals(xml.toString, TrackStatus(xml).toXml.toString)
  }
  
  val practiseSessionDay = PracticeSessionDay(day, track, driver, sessions)
  
  @Test
  def practiseSessionDayRoundTrip() {
    val xml = practiseSessionDay.toXml
    Assert.assertEquals(xml.toString, PracticeSessionDay(xml).toXml.toString)
  }
}