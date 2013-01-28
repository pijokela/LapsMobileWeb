package my.laps.mobile

import java.util.Date
import java.util.Calendar
import java.util.TimeZone

case class Transponder(number : Long)

case class Driver(transponder : Transponder, name : String)

case class Day(year : Int, month : Int, day : Int) {
  override def toString() = "" + year + "-" + month + "-" + day
}

case class PracticeSessionListItem(driver : Driver, sessionTime : Date, sessionDate : Day, passings : Int) {
  val date = sessionTime
  /** A transponder can only have a single session on a single day. */
  def id = sessionDate.toString + "-" + driver.transponder.number.toString
}

case class Lap(durationMs : Long) {
  def fasterThan(other : Lap) = durationMs - other.durationMs <= 0
}

case class LapWithData(lap : Lap, previous : Lap, error : Option[String], bestValid : Lap, worstValid : Lap) {
  def fasterThanPrevious() = lap.fasterThan(previous)
  
  /**
   * Scales the length of this lap compared to other laps. The best lap gets
   * min and the worst gets max and the rest of the laps are in between.
   */
  def scaledLength(min : Int, max : Int) = {
    def best = bestValid.durationMs.toDouble
    def worst = worstValid.durationMs.toDouble
    def length = lap.durationMs.toDouble
   
    val scaled = (length - best) / (worst - best)
    (scaled * (max - min)) + min
  }
}

case class PracticeSession(startDate : Date, laps : List[Lap], validator : LapValidator) {
  private def sortLapsAndTakeHead(lapsList : List[Lap], comp : (Lap,Lap)=>Boolean) = {
    lapsList.sortWith(comp(_,_)).headOption.getOrElse(Lap(-1))
  }
  
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
}

case class PracticeSessionDay(date : Date, track : TrackStatus, driver : Driver, sessions : List[PracticeSession]) {
  def sessionsNewestFirst() = sessions.sortWith((t1, t2)=>t1.startDate.after(t2.startDate))
} 
