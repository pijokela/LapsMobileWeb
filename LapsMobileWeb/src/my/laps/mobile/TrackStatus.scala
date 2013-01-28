package my.laps.mobile

import scala.io.Source

case class Length(value : Double, unit : String)

case class TrackStatus(
    online : Boolean, 
    name : String, 
    location : String, 
    tid : Long, 
    length : Length
)

case class TrackPracticeDay(track : TrackStatus, practiceSessions : List[PracticeSessionListItem] = Nil) {
  lazy val sessionsNewestFirst = practiceSessions.sortWith((t1, t2)=>t1.date.after(t2.date))
}