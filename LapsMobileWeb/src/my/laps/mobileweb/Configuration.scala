package my.laps.mobileweb

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.text.ParseException
import my.laps.mobile.Lap
import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat
import java.text.SimpleDateFormat
import java.text.DateFormatSymbols
import java.util.TimeZone
import my.laps.mobile.Day
import my.laps.mobile.TimeService

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
    val cal = Calendar.getInstance(UserConf.FI)
    cal.setTimeZone(UserConf.TimeZoneFI);
    cal.setTime(date)
    val hoursAndMins = time.split(":").map(_.toInt)
    cal.set(Calendar.HOUR_OF_DAY, hoursAndMins(0))
    cal.set(Calendar.MINUTE, hoursAndMins(1))
    if (hoursAndMins.size > 2)
      cal.set(Calendar.SECOND, hoursAndMins(2))
      
    println("Time: " + time + " parsed to Date object: " + cal.getTime + " on date: " + date)
      
    cal.getTime
  }
  
  def day(date : Date) : Day = {
    val cal = Calendar.getInstance(UserConf.FI)
    cal.setTimeZone(UserConf.TimeZoneFI)
    cal.setTime(date)
    
    Day(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
  }
  
}

/**
 * Configuration options that have been chosen by the user.
 */
class UserConf(val locale : Locale, 
               dateFormat : DateFormat, 
               timeFormat : DateFormat, 
               val formatLap : (Long,Locale)=>String,
               timeService : TimeService) 
{
  
  def formatDate(date : Date) : String = {
    dateFormat.format(date)
  }
  
  def formatDate(day : Day) : String = {
    val cal = Calendar.getInstance(UserConf.FI)
    cal.setTimeZone(UserConf.TimeZoneFI)
    
    cal.set(Calendar.YEAR, day.year)
    cal.set(Calendar.MONTH, day.month)
    cal.set(Calendar.DAY_OF_MONTH, day.day)
    
    dateFormat.format(cal.getTime())
  }
  
  def formatTime(date : Date) : String = {
    timeFormat.format(date)
  }
  
  def lapDuration(lap : Lap) : String = formatLap(lap.durationMs, locale)
  def lapDuration(durationMs : Double) : String = formatLap(durationMs.asInstanceOf[Long], locale)
  
  def toParams = "locale=" + locale.getLanguage() + "-" + locale.getCountry() + "&timeFormat=HH:mm:ss&dateFormat=yyyy-MM-dd&lapTimeFormat=commas"
  
  /**
   * Use the users locale to decide if this date is today:
   */
  def isToday(date : Date) = formatDate(date) == formatDate(timeService.now())
}

object UserConf extends HttpServletRequestParsing {
  
  val FI = new Locale("fi", "FI")
  val TimeZoneFI = TimeZone.getTimeZone("Europe/Helsinki")
  
  val lapTimeFn : Map[String, (Long, Locale)=>String] = 
    Map(
      "ms" -> ((ms:Long, l:Locale)=>"" + ms + " ms"),
      "commas" -> ((ms:Long, l:Locale)=>String.format(l,"%.3f s", java.lang.Double.valueOf(ms/1000.0)).replaceAll("\\.", ",")),
      "dots" -> ((ms:Long, l:Locale)=>String.format(l,"%.3f s", java.lang.Double.valueOf(ms/1000.0)).replaceAll(",", "."))
    )
  
  def createLocale(mime : String) = {
    val parts = mime.split("-")
    new Locale(parts(0), parts(1))
  }

  def parseFromCookies(req : HttpServletRequest, timeService : TimeService) : UserConf = 
    parseWithFunction(cookieValueOption(_, req), timeService)
  
  def parseFromParams(req : HttpServletRequest, timeService : TimeService) : UserConf = 
    parseWithFunction(paramValueOption(_, req), timeService)
  
  def createWithDefaults(timeService : TimeService) : UserConf = 
    parseWithFunction((s)=>None, timeService)
    
  def parseWithFunction(get : (String)=>Option[String], timeService : TimeService) : UserConf = {
    val localeValue = get("locale").getOrElse("fi-FI")
    val timeFormatValue = get("timeFormat").getOrElse("HH:mm:ss")
    val dateFormatValue = get("dateFormat").getOrElse("yyyy-MM-dd")
    val lapTimeFormatValue = get("lapTimeFormat").getOrElse("commas")
    val tz = UserConf.TimeZoneFI
    new UserConf(createLocale(localeValue), 
        DateParser.createFormat(dateFormatValue), 
        DateParser.createFormat(timeFormatValue),
        lapTimeFn(lapTimeFormatValue),
        timeService
    )
  }
}

object DateParser {
  def createFormat(pattern : String) : SimpleDateFormat = {
    val sdf = new SimpleDateFormat(pattern, UserConf.FI)
    sdf.setDateFormatSymbols(DateFormatSymbols.getInstance(Locale.US))
    sdf.setTimeZone(UserConf.TimeZoneFI)
    sdf
  }
}

/**
 * Parse any date found from the practice website.
 */
class DateParser {
  
  private val formats = List(
    DateParser.createFormat("dd MMM, yyyy"),
    DateParser.createFormat("dd MMM yyyy")
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