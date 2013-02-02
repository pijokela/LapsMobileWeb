package my.laps.mobile

import scala.xml._

object Day {
  def apply(string : String) : Day = {
    val parts = string.split('-')
    Day(parts(0).toInt, parts(1).toInt, parts(2).toInt)
  }
  
  def apply(ns : NodeSeq) : Day = apply(ns.text)
}

/**
 * This class makes it easy to have a single date without worrying 
 * about the timezone. This kind of timekeeping is not really exact,
 * but much easier for humans and grouping dates together.
 */
case class Day(year : Int, month : Int, day : Int) {
  override def toString() = "" + year + "-" + month + "-" + day
  def toXml = <day>{toString}</day>
}