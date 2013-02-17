package my.laps.mobile

import java.util.Date
import java.util.Calendar
import java.util.TimeZone
import scala.xml.Elem
import scala.xml.NodeSeq
import java.util.Locale

/**
 * Something that has a unique id. This makes datastore easier to use.
 * This is needed for objects that are stored individually in the datastore.
 */
trait Identifiable {
  def id : String
}

object Transponder {
  def apply(ns : NodeSeq) : Transponder = Transponder((ns \ "number").text.toLong)
}

case class Transponder(number : Long) {
  def toXml = <transponder><number>{number}</number></transponder>
}

object Driver {
  def apply(ns:NodeSeq) : Driver = Driver(transponder = Transponder(ns \ "transponder"), name = (ns \ "name").text)
}

case class Driver(transponder : Transponder, name : String) {
  def toXml = <driver><name>{name}</name>{transponder.toXml}</driver>
}



object PracticeSessionListItem {
  def apply(ns:NodeSeq): PracticeSessionListItem = 
    PracticeSessionListItem(
        Driver(ns\"driver"),
        new Date((ns\"sessionTime").text.toLong),
        Day(ns\"sessionDate"\"day"),
        (ns\"passings").text.toInt
    )
}

case class PracticeSessionListItem(driver : Driver, sessionTime : Date, sessionDate : Day, passings : Int) 
  extends Identifiable 
{
  val date = sessionTime
  /** A transponder can only have a single session on a single day. */
  override def id = sessionDate.toString + "-" + driver.transponder.number.toString
  def toXml = <practiceSessionListItem>
    {driver.toXml}
    <sessionTime>{sessionTime.getTime}</sessionTime>
    <sessionDate>{sessionDate.toXml}</sessionDate>
    <passings>{passings}</passings>
  </practiceSessionListItem>
}


object PracticeSession {
  def apply(ns:NodeSeq):PracticeSession = {
    val validatorE = ns \ "validator"
    val validator = new LapValidator((validatorE \ "minMs").text.toLong, (validatorE \ "maxMs").text.toLong)
    
    PracticeSession(
        startDate = new Date((ns\"startDate").text.toLong),
        laps = (ns\"lap").map(e=>Lap(e)).toList,
        validator = validator
    )
  }
}

case class PracticeSession(startDate : Date, laps : List[Lap], validator : LapValidator) 
{
  private def sortLapsAndTakeHead(lapsList : List[Lap], comp : (Lap,Lap)=>Boolean) = {
    lapsList.sortWith(comp(_,_)).headOption.getOrElse(Lap(-1))
  }
  
  def getDay(tz : TimeZone, l : Locale) = Day(startDate, tz, l)
  
  lazy val validLaps = laps.filter(validator.isValid(_))
  def lapsWithErrors = laps.map(l=>(l, validator.errorMessage(l)))
  lazy val lapsWithData = 	
//    lapsWithErrors.map(p=>LapWithData(p._1, Lap(11000), p._2, bestLapFromValidLaps, worstLapFromValidLaps))
	  lapsWithErrors.zip(worstLapFromAllLaps :: laps).map(p=>LapWithData(p._1._1, p._2, p._1._2, bestLapFromValidLaps, worstLapFromValidLaps))
  
  lazy val averageMsAllLaps = laps.map(_.durationMs).sum / laps.size.asInstanceOf[Double]
  lazy val bestLapFromAllLaps = sortLapsAndTakeHead(laps, _.durationMs < _.durationMs)
  lazy val worstLapFromAllLaps = sortLapsAndTakeHead(laps, _.durationMs > _.durationMs)
  
  lazy val averageMsValidLaps = validLaps.map(_.durationMs).sum / validLaps.size.asInstanceOf[Double]
  lazy val bestLapFromValidLaps = sortLapsAndTakeHead(validLaps, _.durationMs < _.durationMs)
  lazy val worstLapFromValidLaps = sortLapsAndTakeHead(validLaps, _.durationMs > _.durationMs)
  
  def toXml = 
    <practiceSession>
      <startDate>{startDate.getTime}</startDate>
      <validator><minMs>{validator.minMs}</minMs><maxMs>{validator.maxMs}</maxMs></validator>
      {laps.map(_.toXml)}
    </practiceSession>
}



object PracticeSessionDay {
  def apply(ns:NodeSeq) : PracticeSessionDay = 
    PracticeSessionDay(
      Day(ns\"day"),
      TrackStatus(ns\"trackStatus"),
      Driver(ns\"driver"),
      (ns\"practiceSession").map(s=>PracticeSession(s)).toList
    )
}

case class PracticeSessionDay(day : Day, track : TrackStatus, driver : Driver, sessions : List[PracticeSession]) 
  extends Identifiable 
{
  override def id = day.toString + "_" + track.tid + "_" + driver.transponder.number
  def sessionsNewestFirst() = sessions.sortWith((t1, t2)=>t1.startDate.after(t2.startDate))
  def toXml = <practiceSessionDay>{day.toXml}{track.toXml}{driver.toXml}{sessions.map(_.toXml)}</practiceSessionDay>
} 
