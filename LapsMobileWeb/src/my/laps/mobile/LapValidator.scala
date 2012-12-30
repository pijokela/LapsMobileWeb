package my.laps.mobile

class LapValidator(val minMs : Long, val maxMs : Long) {
  val tests = List(
      (lap:Lap)=>if (lap.durationMs < minMs) Some("Lap was below minimum duration of " + minMs + " ms.") else None,
      (lap:Lap)=>if (lap.durationMs > maxMs) Some("Lap was above maximum duration of " + maxMs + " ms.") else None
  )
  
  def isValid(lap : Lap) : Boolean = !tests.exists(_(lap) != None)
  /**
   * Find first error message or None.
   */
  def errorMessage(lap : Lap) : Option[String] = 
    tests.map(_(lap)).filter(_ != None).map(_.get).headOption
}