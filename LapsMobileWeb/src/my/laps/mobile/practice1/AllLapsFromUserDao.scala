package my.laps.mobile.practice1

import my.laps.mobileweb.MylapsConf
import scala.io.Source
import scala.io.Codec
import my.laps.mobile.Transponder
import my.laps.mobile.PracticeSession
import my.laps.mobile.Lap
import my.laps.mobile.LapValidator
import java.util.Date


/**
 * http://www.mylaps.com/practice/GetTXTFile.jsp?transponder=5916939&tid=1429
 */
class AllLapsFromUserOnTrackDao(urlBase : String, conf : MylapsConf = new MylapsConf()) {
  private def url(tid : Long, tp : Transponder) = urlBase + "/practice/GetTXTFile.jsp?transponder=" + tp.number + "&tid=" + tid

  private def sourceFromUrlString(url : String) = Source.fromURL(url)(Codec.UTF8)
  
  def getAllPractiseSessionDaysForTransponderOnTrack(tid : Long, tp : Transponder) : List[PracticeSession] = {
    val urlString = url(tid, tp)
    val source = sourceFromUrlString(urlString)
    val parser = try {
      new AllPracticeSessionsParser(source, conf)
    }
    catch {
      case e : Exception => throw new RuntimeException("Failed to load web page from: " + urlString, e)
    }
    finally {
      source.close
    }
    parser.parsePracticeSessions
  }
}

class AllPracticeSessionsParser(source : Source, conf : MylapsConf) {
  
  def extractPracticeSessions(lines : List[String]) : List[PracticeSession] = {
    val startIndex = lines.indexWhere(_.contains("First passing"))
    if (startIndex == -1) return Nil
    
    val linesSessionStart = lines.drop(startIndex)
    val startLine = linesSessionStart.head
    val lapLines = linesSessionStart.tail.takeWhile(s=>(!s.isEmpty() && !s.contains("First passing")))
    val laps = lapLines.map(parseLapDuration(_))
    
    val session = PracticeSession(parseDate(startLine), laps, new LapValidator(5000, 25000))
    session :: extractPracticeSessions(lines.drop(startIndex + lapLines.size + 1))
  }
  
  def parseDate(str : String) : Date = {
    val parts = str.split("\\s")
    val dateStr = parts(1)
    val timeStr = parts(2)
    conf.parseDate(dateStr + "T" + timeStr)
  }
  
  /**
   * 5916939	2012-12-28	17:25:50	00:09.267
   */
  def parseLapDuration(str : String) = {
    val timeSection = str.split("\\s").reverse.head
    val timeParts = "(\\d\\d):(\\d\\d).(\\d\\d\\d)".r.unapplySeq(timeSection)
    val time = timeParts.map(t=>60*1000*t(0).toLong + 1000*t(1).toLong + t(2).toLong)
    time.map(Lap(_)).getOrElse(Lap(-1))
  }
  
  def parsePracticeSessions() : List[PracticeSession] = {
    val linesWithTimes = source.getLines.drop(1).toList
    val r = extractPracticeSessions(linesWithTimes)
    r
  }
}