package my.laps.mobile

import java.util.Locale
import my.laps.mobileweb.UserConf
import my.laps.mobileweb.UserConf
import my.laps.mobileweb.UserConf
import java.util.Date

class Html(val conf : UserConf) {
	val header = """<!DOCTYPE html>
	  <html>
	    <head>
	      <title>My Laps Mobile</title>
	      <link rel="stylesheet" href="style.css" />
	      <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
	    </head>
	    <body>
	  """
	  
	val footer = """
	    <h1><a href="?">Change track</a></h1>
        <script src="scripts.js"></script>
	    </body>
	  </html>
	  """
	  
	def selectTrackPage(tid : Long, validator : LapValidator, conf : UserConf) = """
	    <h1>Select Track</h1>
	    Select the track to get practice results from. Use the main website to find the track id.
	    <form method="GET">
	      <input type="text" name="tid" value="""" + tid + """" size="4" maxlength="4" />
	      <input type="submit" value="Select Track" />
	    </form>
	    <h1>Configure lap validation</h1>
  	    <form method="POST">
	    <p>Currently the shortest allowed lap is """ + validator.minMs + 
	    " and the longest allowed laptime is " + validator.maxMs + """.</p>
	      <div>Shortest allowed laptime <input type="text" name="minMs" value=""""  + validator.minMs + """" maxlength="6" size="5" /> ms.</div>
	      <div>Longest allowed laptime <input type="text" name="maxMs" value=""""  + validator.maxMs + """" maxlength="6" size="5" /> ms.</div>
	      
	    <h1>Configure time formats</h1>
	    <p>Lap times are shown like """ + conf.lapDuration(Lap(19876L)) + """.</p>
	    
	      <div>
            <input type="radio" name="lapTimeFormat" value="ms">19876 ms - whole numbers, milliseconds
	        <br>
            <input type="radio" name="lapTimeFormat" value="commas">19,876 s - seconds with comma as decimal separator
	        <br>
            <input type="radio" name="lapTimeFormat" value="dots">19.876 s - seconds with dot as decimal separator
          </div>
	    
	    <p>Todays date is """ + conf.formatDate(new Date()) + """.</p>
	    
	      <div>
            <input type="radio" name="dateFormat" value="yyyy-MM-dd">2012-12-30
	        <br>
            <input type="radio" name="dateFormat" value="yyyy.MM.dd">2012.12.30
	        <br>
            <input type="radio" name="dateFormat" value="dd.MM.yyyy">30.12.2012
	        <br>
            <input type="radio" name="dateFormat" value="MM/dd/yyyy">12/30/2012
          </div>
	    
	    <p>Current time is """ + conf.formatTime(new Date()) + """.</p>

	      <div>
            <input type="radio" name="timeFormat" value="HH:mm:ss">14:30:43
	        <br>
            <input type="radio" name="timeFormat" value="HH.mm.ss">14.30.43
          </div>
	    <p>Click here to update all the configuration options. The radio buttons can be left empty to not select a new value.</p>
        <input type="submit" value="Update configuration" />
	  </form>
	  """
	  
	def driverSummary(driver : Driver) = """
	  <h2>""" + driver.name + """</h2>
	  <div>Transponder: """ + driver.transponder.number + """</div>"""
	  
	def trackSummary(track : TrackStatus) = """
	    <h1><a href="?tid=""" + track.tid + """">""" + track.name + """</a></h1>
	    <div>Length: """ + track.length.value + " " + track.length.unit + """</div>
	    <div>Location: """ + track.location + """</div>
	    <div>Online: """ + (if (track.online) "Track is online!" else "No") + """</div>
	  """
	  
	def practiceSessionSummary(tid : Long, sessionListItem : PracticeSessionListItem) = """
	  <div><a href="?tid=""" + tid + "&transponder=" + sessionListItem.driver.transponder.number + """">""" + 
	  sessionListItem.driver.name + """</a> from """ + conf.formatDate(sessionListItem.date) + """</div>
	  """
	  
	def trackTrainingDay(day : TrackPracticeDay) = trackSummary(day.track) + """
	    <h1>Latest results</h1>
	""" + day.sessionsNewestFirst.map(practiceSessionSummary(day.track.tid, _)).mkString("\n")
	
	def sessionLap(lap : LapWithData) : String = 
	  (lap.fasterThanPrevious, lap.error) match {
	    case (_, Some(e)) => """<li class="invalid lap">""" + conf.lapDuration(lap.lap) + " - " + e + "</li>\n"
	    case (true, None) => """<li class="valid lap faster" style="border-right-width: """ + lap.scaledLength(4,10) + """em;">""" + conf.lapDuration(lap.lap) + "</li>\n"
	    case (false, None) => """<li class="valid lap slower" style="border-right-width: """ + lap.scaledLength(4,10) + """em;">""" + conf.lapDuration(lap.lap) + "</li>\n"
	  }
	
	def sessionLapSection(session : PracticeSession) = 
	  """<h1>Session """ + conf.formatTime(session.startDate) + """</h1>""" + 
	  "<ul><li>Total laps: " + session.laps.size + "</li>" +
	  "<ul><li>Best lap: " + conf.lapDuration(session.bestLapFromAllLaps) + "</li>" +
	  "<li>Average lap: " + conf.lapDuration(session.averageMsAllLaps) + "</li>" +
	  "<li>Slowest lap: " + conf.lapDuration(session.worstLapFromAllLaps) + "</li>" +
	  "</ul>" + 
	  "<li>Valid laps: " + session.validLaps.size + "</li>" +
	  "<ul><li>Best lap: " + conf.lapDuration(session.bestLapFromValidLaps) + "</li>" +
	  "<li>Average lap: " + conf.lapDuration(session.averageMsValidLaps) + "</li>" +
	  "<li>Slowest lap: " + conf.lapDuration(session.worstLapFromValidLaps) + "</li>" +
	  "</ul>" + 
	  "</ul>" + 
	  """<h2>Session laps</h2>
	  <div class="hiddenLapList"><span class="showButton">... show ...</span>
	  <ol>""" + session.lapsWithData.map(sessionLap(_)).mkString("\n") + """</ol></div>"""
	
	def transponderSessions(practiceSessionDay : PracticeSessionDay) = trackSummary(practiceSessionDay.track) + """
	    <h2>Results from """ + conf.formatDate(practiceSessionDay.date) + """</h2>
	  """ + driverSummary(practiceSessionDay.driver) + """
	  """ + practiceSessionDay.sessions.map(sessionLapSection(_)).mkString("\n")
}