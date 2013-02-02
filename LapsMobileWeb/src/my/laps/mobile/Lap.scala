package my.laps.mobile

import scala.xml.NodeSeq


object Lap {
  def apply(ns:NodeSeq):Lap=Lap(ns.text.toLong)
}

case class Lap(durationMs : Long) {
  def fasterThan(other : Lap) = durationMs - other.durationMs <= 0
  def toXml = <lap>{durationMs}</lap>
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
