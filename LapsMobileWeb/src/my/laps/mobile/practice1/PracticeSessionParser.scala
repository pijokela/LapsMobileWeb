package my.laps.mobile.practice1

import scala.io.Source
import java.util.Date
import my.laps.mobileweb.MylapsConf
import my.laps.mobileweb.MylapsConf
import my.laps.mobile.Driver
import my.laps.mobile.Lap
import my.laps.mobile.LapValidator
import my.laps.mobile.PracticeSession
import my.laps.mobile.PracticeSessionDay
import my.laps.mobile.TrackStatus
import my.laps.mobile.Transponder

class PracticeSessionParser(track : TrackStatus, source : Source, validator : LapValidator, conf : MylapsConf) {

  val lines = source.getLines.toList
  
  lazy val confidential = lines.find(_.contains("The laptimes of this transponder have been made confidential by the owner.")).isDefined
  
  lazy val parseTrackPracticeDay = if(confidential) {
    PracticeSessionDay(conf.day(new Date()), track, Driver(Transponder(-1), "Results are confidential"), Nil)
  } else {
    PracticeSessionDay(conf.day(parseDate), track, Driver(parseTransponder, parseDriverName), parsePracticeSesssions)
  }
  
  lazy val parsePracticeSesssions : List[PracticeSession] = {
    val tables = findLapTables(lines)
    tables.map(parsePracticeSession(_))
  }
  
  /** <strong>Transponder</strong> */
  private val transponderHeaderRegex = "<strong>Transponder</strong>".r
  /** <td>(\\d+)</td> */
  private val transponderNumberRegex = "<td>(\\d+)</td>".r
  
  lazy val parseTransponder : Transponder = {
    val headerIndex = lines.indexWhere(l=>transponderHeaderRegex.findFirstIn(l).isDefined)
    if (headerIndex == -1) throw new IllegalArgumentException("Cannot find transponder header from web page.")
    val transponderLine = lines(headerIndex + 1)
    val m = transponderNumberRegex.findFirstMatchIn(transponderLine).getOrElse(throw new IllegalStateException("Cannot parse transponder number from line: " + transponderLine))
    Transponder(m.group(1).toLong)
  }
  
  lazy val parseDriverName = {
    val parser = new DriverNameParser()
    val nameLine = lines.find(l=>parser.parse(l).isDefined).getOrElse(throw new IllegalArgumentException("Could not find driver name from web page."))
    parser.parse(nameLine).get
  }
  
  /** <span class="hide">Results of 29 Dec 2012</span> */
  private val dateRegex = "<span class=\"hide\">Results of ([0-9A-Za-z ]+)</span>".r
  
  lazy val parseDate = {
    val dateLine = lines.find(l=>dateRegex.findFirstIn(l).isDefined).getOrElse(throw new IllegalArgumentException("Could not find date from web page."))
    val m = dateRegex.findFirstMatchIn(dateLine).get
    conf.parseDate(m.group(1))
  }
  
  /** <table class="mylaps practice" cellspacing="0" summary="Practice lap times"> */
  private val lapTableStartRegex = "summary=\"Practice lap times\"".r
  private val lapTableEndRegex = "</table>".r

  /**
   * Parse the practice session lap tables from the web page. The tables can be parsed as XML.
   */
  def findLapTables(lines : List[String]) : List[List[String]] = {
    val headerIndex = lines.indexWhere(l=>lapTableStartRegex.findFirstIn(l).isDefined)
    if (headerIndex == -1) {
      Nil
    } else {
      val tableAndEnd = lines.drop(headerIndex)
      val table = tableAndEnd.takeWhile(line=>(!lapTableEndRegex.findFirstIn(line).isDefined))
      table :: findLapTables(tableAndEnd.drop(table.size))
    }
  }
  
  private val sessionStartDateRegex = "<caption>Session \\d+ started at (\\d\\d:\\d\\d:\\d\\d) <a id=\"[A-Za-z0-9 ]+\"></a></caption>".r
  def parseSessionStartDate(lines : List[String]) = {
    val dateLine = lines.find(l=>sessionStartDateRegex.findFirstIn(l).isDefined).getOrElse(throw new IllegalArgumentException("Could not find start date from session."))
    val m = sessionStartDateRegex.findFirstMatchIn(dateLine).get
    conf.dateWithTime(parseDate, m.group(1))
  }
  
  private val lapTimeRegex = "<td class=\"time .*?\">(\\d+\\.\\d+)</td>".r
  
  /**
   * <td class="time ">9.528</td>
   */
  def parseLapTimes(tableLines : List[String]) : List[Lap] = {
    val x = tableLines.map(l=>lapTimeRegex.unapplySeq(l)).filter(_ != None).map(_.get)
    val doubleMsLaps = x.map(_(0).toDouble * 1000)
    doubleMsLaps.map(d=>Lap(d.toLong))
  }
  
  def parsePracticeSession(tableLines : List[String]) : PracticeSession = {
    val startTime = parseSessionStartDate(tableLines)
    val laps = parseLapTimes(tableLines)
    PracticeSession(startTime, laps, validator)
  }
  
  
}