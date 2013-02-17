package my.laps.mobile.practice1

class DriverNameParser {
  /** <span class="hide">Laptimes for Name of Driver</span> */
  private val nameRegex = "<span class=\"hide\">Laptimes for ((?:\\p{L}|\\s|\\d|[\\.;:'])+)</span>".r
  
  val transponderNumberStart = "Transponder "
  
  def parse(line : String) = {
    val nameOnPage = nameRegex.findFirstMatchIn(line).map(_.group(1))
    nameOnPage.map(n=>if(n.startsWith(transponderNumberStart))n.substring(transponderNumberStart.length()) else n)
  }
}

