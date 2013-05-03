package my.laps.mobile.practice1

import scala.io.Source
import scala.xml.XML
import scala.xml.Node
import java.util.Date
import my.laps.mobileweb.MylapsConf
import my.laps.mobile.Driver
import my.laps.mobile.Length
import my.laps.mobile.PracticeSessionListItem
import my.laps.mobile.TrackPracticeDay
import my.laps.mobile.TrackStatus
import my.laps.mobile.Transponder

/**
 * The parameter should be the page from this URL:
 * http://www.mylaps.com/practice/showTrack.jsp?tid=1429
 */
class TrackStatusParser(source : Source, conf : MylapsConf) {
  
  val lines = source.getLines.toList
  
  
  val trackNameRegex = "<h2>(.+?)</h2>".r
  
  /**
   * <h1><img src="http://www.mylaps.com/textimage/headerDark/h1/TamUA%253An%2Bmattorata.gif" alt="TamUA:n mattorata"/><span class="hide">TamUA:n mattorata</span></h1>
   */
  private def parseTrackName(line : String) : Option[String] = {
    val m = trackNameRegex.findFirstMatchIn(line)
    m.map(_.group(1))
  }
  
  lazy val trackName : String = {
    val lineOption = lines.find(parseTrackName(_).isDefined)
    lineOption.map(parseTrackName(_).get).getOrElse("No track name")
  }
  
  val tidRegex = "<input type=\"hidden\" name=\"tid\" value=\"(\\d+?)\"".r
  
  /**
   * 	   	<input type="hidden" name="tid" value="1429">
   */
  private def parseTid(line : String) : Option[String] = {
    val m = tidRegex.findFirstMatchIn(line)
    m.map(_.group(1))
  }
  lazy val tid : Long = {
    val lineOption = lines.find(parseTid(_).isDefined)
    lineOption.map(parseTid(_).get).map(_.toLong).getOrElse(-1000)
  }
  
  lazy val trackSection = lines
		  .dropWhile(!_.trim.equals("<table>"))
		  .takeWhile(!_.trim.equals("</div>"))
  
  /**

    <h2>TamUA:n mattorata</h2>
    <div class="info">
        <!-- div class="logo">
        <a href="http://www.tamua.fi/" target="_blank"><img id="idmtracklogo" src="/images/livetracks/track1429.png" alt="TamUA:n mattorata" width="120" height="90" /></a>

        </div -->
        <table>
            <tbody>
                <tr>
                    <td><strong>Length</strong></td>
                    <td>0.07 km</td>
                </tr>
                <tr>
                    <td><strong>Location</strong></td>
                    <td>Nokia</td>
                </tr>
                <tr>
                    <td><strong>Status</strong></td>
                    <td>Online</td>
                </tr>
                <tr>
                    <td><strong>Active</strong></td>
                    <td>No track activity</td>
                </tr>
            </tbody>
        </table>
    </div>
   */
  
  lazy val parseTrackStatus = {
    val trackElem = XML.loadString(trackSection.mkString(""))
    val rows = trackElem \\ "tr"
    val nameValuePairs = rows.map(n=>parseTr(n))
    val nameValueMap = nameValuePairs.toMap

    TrackStatus(
        nameValueMap("Status") == "Online",
        trackName,
        nameValueMap("Location"),
        tid,
        parseLength(nameValueMap("Length"))
    )
  }

		  
  lazy val parseTrackPracticeDay : TrackPracticeDay = {
    TrackPracticeDay(
        parseTrackStatus,
        parsePracticeSessions()
    )
  }
  
  /**
   * Parse a TR with two TD elements to a tuple.
   * <tr>
	   <td><strong>Active</strong></td>
	   <td>No track activity</td>
     </tr>
   */
  private def parseTr(node : Node) : (String, String) = {
    val tdSeq = node \ "td"
    (tdSeq(0).text, tdSeq(1).text)
  }
  
  private def parseLength(combinedString : String) = {
    val parts = combinedString.split(" ")
    Length(parts(0).toDouble, parts(1))
  }
  
  /** <div class="col-2"><table class="mylaps results" cellspacing="0" summary="MyLaps Practice transponders"> */
  val practiceSessionsTableStartRegex = "<table.+class=\"mylaps results\"".r
  private def isStartOfPracticeSession(line : String) = {
    val r = practiceSessionsTableStartRegex.findFirstIn(line).isDefined
    if (r) println("Found start line: " + line)
    r
  }
  
  private def parsePracticeSessions() : List[PracticeSessionListItem] = {
    val lineWithIndex = lines.zipWithIndex
    val startIndexList = lineWithIndex.filter(p=>isStartOfPracticeSession(p._1)).map(_._2)
    val practiceSessionLinesList = startIndexList.map(extractPracticeSessionLines(_))
    practiceSessionLinesList.flatMap(parsePracticeSessions(_))
  }

  /** </div> */
  val practiceSessionsColEndRegex = "</div>".r
  private def isEndOfPracticeSessionCol(line : String) = 
    practiceSessionsColEndRegex.findFirstIn(line).isDefined

  private def extractPracticeSessionLines(startLineIndex : Int) : List[String] = {
    val practiceSessionAndEnd = lines.drop(startLineIndex)
    practiceSessionAndEnd.takeWhile(!isEndOfPracticeSessionCol(_))
  }
  
  /**
					<table cellspacing="0" summary="MyLaps Practice transponders" class="mylaps results">
<colgroup>
	<col class="tx"/>
	<col class="time"/>
	<col class="passings"/>
</colgroup>
<thead><tr>	<td>Transponder</td>	<td class="time">Time</td>	<td class="laps">Passings</td></tr>
</thead><tbody><tr>	<td colspan="3" class="date">23 Dec, 2012</td></tr>
<tr class="odd">	<td><a href="showLaptimes.jsp?tid=1429&transponder=5778155">Pauli Helin</a></td>	<td class="time">15:31</td>	<td class="laps">4176</td></tr>
<tr class="even">	<td><a href="showLaptimes.jsp?tid=1429&transponder=2894350">Henri Eskman</a></td>	<td class="time">14:39</td>	<td class="laps">3</td></tr>
</tbody>
</table>
  */
  private def parsePracticeSessions(lines : List[String]) : List[PracticeSessionListItem] = {
    val xmlString = lines.mkString("\n").replaceAll("&", "&amp;")
    println(xmlString)
    val xml = XML.loadString(xmlString)
    val rows = xml \\ "tbody" \\ "tr";
    val startValue = (new Date(), List[PracticeSessionListItem]())
    val result : (Date, List[PracticeSessionListItem]) = 
      rows.foldLeft(startValue)(parsePracticeSessionTr(_, _)
    )
    result._2
  }
  
  private def parsePracticeSessionTr(prev : (Date, List[PracticeSessionListItem]), node : Node) = {
    val cellSeq = node \ "td"
    if (cellSeq.size == 1) {
      (conf.parseDate(cellSeq.head.text), prev._2)
    } else {
      val date = conf.dateWithTime(prev._1, cellSeq(1).text)
      val day = conf.day(prev._1)
      val driver = parseDriver(cellSeq.head.toString)
      (prev._1, PracticeSessionListItem(driver, date, day, cellSeq(2).text.toInt) :: prev._2)
    }
  }
  
  // <td><a href="showLaptimes.jsp?tid=1430&amp;transponder=2204066">N.O.X.</a></td>
  // <td><a href="/profiles/index.jsp?id=105493"><img class="profile" src="/images/icons/competitor.png"></img></a><a href="showLaptimes.jsp?tid=1430&amp;transponder=7625356">Exotek TCX</a></td>
  private val driverRegex = "<td>.*?<a href=\"showLaptimes.jsp\\?tid=(\\d+)\\&amp;transponder=(\\d+)\">(.+?)</a></td>".r
  
  /**
   * <a href="showLaptimes.jsp?tid=1429&transponder=2894350">Henri Eskman</a>
   */
  private def parseDriver(line : String) : Driver = {
    val groups = driverRegex.unapplySeq(line.trim).getOrElse(throw new IllegalArgumentException("Cannot parse driver from: '" + line + "'"))
    Driver(Transponder(groups(1).toLong), groups(2))
  }
}
