package my.laps.mobileweb

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.text.ParseException
import my.laps.mobile.Lap

/**
 * Configuration that makes reading the mylaps website easier.
 */
class MylapsConf {
  
  private def mylapsDateParser() : DateParser = new DateParser
  
  def parseDate(str : String) = mylapsDateParser.parse(str)
  
  def parseHoursMins(time : String) : (Int, Int) = {
    val parts = time.split(":").map(_.toInt)
    (parts(0), parts(1))
  }

  def parseHoursMinsSecs(time : String) : (Int, Int, Int) = {
    val parts = time.split(":").map(_.toInt)
    (parts(0), parts(1), parts(2))
  }

  def dateWithTime(date : Date, time : String) = {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    val hoursAndMins = time.split(":").map(_.toInt)
    cal.set(Calendar.HOUR_OF_DAY, hoursAndMins(0))
    cal.set(Calendar.MINUTE, hoursAndMins(1))
    if (hoursAndMins.size > 2)
      cal.set(Calendar.SECOND, hoursAndMins(2))
    cal.getTime
  }
}

/**
 * Configuration options that have been chosen by the user.
 */
class UserConf {
  
  lazy val locale = new Locale("fi", "FI")
  
  def formatDate(date : Date) : String = {
    val f = new SimpleDateFormat("yyyy-MM-dd", locale)
    f.format(date)
  }
  
  def formatTime(date : Date) : String = {
    val f = new SimpleDateFormat("HH:mm:ss", locale)
    f.format(date)
  }
  
  def lapDuration(lap : Lap) : String = "" + lap.durationMs + " ms"
  def lapDuration(durationMs : Double) : String = "" + durationMs.asInstanceOf[Long] + " ms"
}

/**
 * Parse any date found from the practice website.
 */
class DateParser {
  private val formats = List(
    new SimpleDateFormat("dd MMM, yyyy", Locale.US),
    new SimpleDateFormat("dd MMM yyyy", Locale.US)
  )

  def parse(string : String) : Date = {
    parse(string, formats)
  }
  
  private def parse(string : String, formats : List[DateFormat]) : Date = 
    try {
      val format = formats.headOption.getOrElse(throw new IllegalArgumentException("Cannot parse date from: " + string))
      format.parse(string)
    } catch {
      case e : ParseException => parse(string, formats.tail)
	}
}