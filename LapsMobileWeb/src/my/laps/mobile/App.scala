package my.laps.mobile

import scala.io.Source
import java.text.SimpleDateFormat
import java.util.Locale
import java.text.DateFormat
import java.text.DateFormat
import java.util.Date
import java.text.DateFormat
import java.text.ParseException
import java.util.Date
import java.util.Date
import java.util.Calendar
import java.util.Date
import java.util.Locale

object App {
  def main(args : Array[String]) {
    val s = Source.fromURL("http://www.mylaps.com/practice/showTrack.jsp?tid=1429")
    println(s.getLines.mkString("\n"))
  }
  
  def mylapsDateParser() : DateParser = new DateParser
  
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
  
  def formatDate(date : Date, locale : Locale) : String = {
    val f = new SimpleDateFormat("yyyy-MM-dd", locale)
    f.format(date)
  }
  
  def formatTime(date : Date, locale : Locale) : String = {
    val f = new SimpleDateFormat("HH:mm:ss", locale)
    f.format(date)
  }
  
}

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