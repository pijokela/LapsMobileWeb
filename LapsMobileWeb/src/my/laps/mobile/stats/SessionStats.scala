package my.laps.mobile.stats

import scala.xml.Elem
import my.laps.mobile.PracticeSession
import my.laps.mobile.Lap
import my.laps.mobile.LapValidator
import my.laps.mobileweb.UserConf

trait SessionStats {
  /**
   * A title for the whole Stats object.
   */
  def title : String
  /**
   * A list of table stuff for this object. The name must be a String, but the value part can contain simple HTML.
   */
  def nameValuePairs(session : PracticeSession) : List[(String, Elem)]
}

case class Trio(startLap : Int, laps : List[Lap]) {
  val duration = laps.map(_.durationMs).sum
  def averageDuration = duration / 3.0
  def bestLapDuration = laps.sortWith(_.durationMs < _.durationMs).head.durationMs
  def endLap = startLap + 2
  
  def chooseFaster(other : Option[Trio]) : Trio =
    List(Some(this), other).flatten.sortWith(_.duration < _.duration).head
}

class SetOfThreeBestLaps(conf : UserConf) extends SessionStats {
  val title = "Best Three Consequtive Laps"
  def nameValuePairs(session : PracticeSession) : List[(String, Elem)] = {
    val trioOpt : Option[Trio] = findFastestValidTrio(session.laps, session.validator)
    trioOpt match {
      case Some(t) => 
        ("Laps", <span>{t.startLap} to {t.endLap}; total duration {conf.lapDuration(t.duration)}.</span>) ::
        ("Best in set", <span>{conf.lapDuration(t.bestLapDuration)} with {conf.lapDuration(t.averageDuration)} average.</span>) ::
        Nil
      case None => ("Laps", <span>No set of laps found in session.</span>) :: Nil
    }
  }
  
  def findFastestValidTrio(laps : List[Lap], validator : LapValidator, startLap : Int = 0) : Option[Trio] = {
    val current = createTrio(laps, validator, startLap)
    laps match {
      case l if l.size < 3 => current
      case head :: l => findFastestValidTrio(l, validator, startLap + 1) match {
        case None => current
        case Some(t) => Some(t.chooseFaster(current))
      }
      case Nil => current // This makes the compiler happy.
    }
  }
  
  def createTrio(laps : List[Lap], validator : LapValidator, startLap : Int) : Option[Trio] = 
    laps match {
      case l1 :: l2 :: l3 :: moreLaps if (validator.isValid(l1) && validator.isValid(l2) && validator.isValid(l3)) => 
        Some(Trio(startLap, List(l1, l2, l3)))
      case _ => None
    }
    
}