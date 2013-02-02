package my.laps.mobile

import java.util.Date
import java.text.SimpleDateFormat

trait TimeService {
  def now() : Date
}

class RealTimeService extends TimeService {
  override def now() = new Date()
}

/**
 * It's easier to set time in tests with this String factory method.
 */
object TestTimeService {
  def apply(str : String) = {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.Z")
    new TestTimeService(format.parse(str))
  }
}

class TestTimeService(now : Date) extends TimeService {
  override def now() = now
}