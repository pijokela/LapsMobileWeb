package my.laps.mobile

import my.laps.mobileweb.HttpServletRequestParsing
import javax.servlet.http.HttpServletRequest
import my.laps.mobileweb.MyHttpRequest

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

object LapValidator extends HttpServletRequestParsing {
  def createFromCookie(req : MyHttpRequest) : LapValidator = 
    cookieOption("minMs-maxMs", req).map(
      cookie=>{
        val parts = cookie.getValue().split("-")
		val minMs = parts(0).toLong
		val maxMs = parts(1).toLong
		new LapValidator(minMs, maxMs)
      }).getOrElse(new LapValidator(5000, 25000))
      
  def createFromParam(req : MyHttpRequest) : LapValidator = 
    paramValueOption("minMs-maxMs", req).map(
      value=>{
        val parts = value.split("-")
		val minMs = parts(0).toLong
		val maxMs = parts(1).toLong
		new LapValidator(minMs, maxMs)
      }).getOrElse(new LapValidator(5000, 25000))
}