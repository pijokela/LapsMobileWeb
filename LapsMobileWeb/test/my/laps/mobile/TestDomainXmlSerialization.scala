package my.laps.mobile

import org.junit._

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
}