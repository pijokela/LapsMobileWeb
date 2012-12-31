package my.laps.mobile

import java.util.Locale
import my.laps.mobileweb.UserConf
import my.laps.mobileweb.UserConf

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
	  
	def selectTrackPage(tid : Long, validator : LapValidator) = """
	    <h1>Select Track</h1>
	    Select the track to get practice results from. Use the main website to find the track id.
	    <form method="GET">
	      <input type="text" name="tid" value="""" + tid + """" size="4" maxlength="4" />
	      <input type="submit" value="Select Track" />
	    </form>
	    <h1>Configure lap validation</h1>
	    <p>Currently the shortest allowed lap is """ + validator.minMs + 
	    " and the longest allowed laptime is " + validator.maxMs + """.</p>
	    <form method="POST">
	      <div>Shortest allowed laptime <input type="text" name="minMs" value=""""  + validator.minMs + """" maxlength="6" size="5" /> ms.</div>
	      <div>Longest allowed laptime <input type="text" name="maxMs" value=""""  + validator.maxMs + """" maxlength="6" size="5" /> ms.</div>
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
	
	def sessionLap(lap : Lap, error : Option[String]) : String = 
	  error match {
	    case Some(e) => """<li class="invalid">""" + conf.lapDuration(lap) + " - " + e + "</li>\n"
	    case None => """<li class="valid">""" + conf.lapDuration(lap) + "</li>\n"
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
	  <ol>""" + session.lapsWithErrors.map(pair=>sessionLap(pair._1, pair._2)).mkString("\n") + """</ol></div>"""
	
	def transponderSessions(practiceSessionDay : PracticeSessionDay) = trackSummary(practiceSessionDay.track) + """
	    <h2>Results from """ + conf.formatDate(practiceSessionDay.date) + """</h2>
	  """ + driverSummary(practiceSessionDay.driver) + """
	  """ + practiceSessionDay.sessions.map(sessionLapSection(_)).mkString("\n")
}