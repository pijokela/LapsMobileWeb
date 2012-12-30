package my.laps.mobile

import java.util.Date

case class Transponder(number : Long)

case class Driver(transponder : Transponder, name : String)

case class PracticeSessionListItem(driver : Driver, date : Date, passings : Int)

case class Lap(durationMs : Long)

case class PracticeSession(startDate : Date, laps : List[Lap], validator : LapValidator) {
  private def sortLapsAndTakeHead(lapsList : List[Lap], comp : (Lap,Lap)=>Boolean) = {
    lapsList.sortWith(comp(_,_)).headOption.getOrElse(Lap(-1))
  }
  
  lazy val validLaps = laps.filter(validator.isValid(_))
  lazy val lapsWithErrors = laps.map(l=>(l, validator.errorMessage(l)))
  
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
