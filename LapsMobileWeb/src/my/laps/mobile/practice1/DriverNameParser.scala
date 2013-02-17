package my.laps.mobile.practice1

class DriverNameParser {
  /** <span class="hide">Laptimes for Name of Driver</span> */
  private val nameRegex = "<span class=\"hide\">Laptimes for ((?:\\p{L}|\\s|\\d|[\\.;:'])+)</span>".r
  
  def parse(line : String) = 
    nameRegex.findFirstMatchIn(line).map(_.group(1))
}

