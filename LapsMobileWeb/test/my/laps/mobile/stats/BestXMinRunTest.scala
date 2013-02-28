package my.laps.mobile.stats

import org.junit.Test
import my.laps.mobile.Lap
import my.laps.mobileweb.UserConf
import my.laps.mobile.TimeService
import my.laps.mobile.TestTimeService
import java.util.Date
import org.junit.Assert

class BestXMinRunTest {
  
  val constantLaps = List(Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000))
  val variableLaps = List(Lap(12000), Lap(12000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(10000), Lap(12000), Lap(10000))

  @Test
  def constantLapsTest() {
    val runStats = new BestXMinRun(UserConf.createWithDefaults(new TestTimeService(new Date())), 1)
    val res = runStats.calculateBestResult(constantLaps, 0)
    Assert.assertEquals(0, res._1.startLap)
    Assert.assertEquals(6, res._1.laps.size)
    Assert.assertEquals(0, res._2)
  }

  @Test
  def variableLapsTest() {
    val runStats = new BestXMinRun(UserConf.createWithDefaults(new TestTimeService(new Date())), 1)
    val res = runStats.calculateBestResult(variableLaps, 0)
    Assert.assertEquals(6, res._1.laps.size)
    Assert.assertEquals(0, res._2)
    Assert.assertEquals(2, res._1.startLap)
  }
}