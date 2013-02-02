package my.laps.mobile

import scala.io.Source
import scala.xml.Elem
import scala.xml.NodeSeq

object Length {
  def apply(ns:NodeSeq):Length = Length((ns \ "value").text.toDouble,(ns \ "unit").text)
}

case class Length(value : Double, unit : String) {
  def toXml = <length><value>{value}</value><unit>{unit}</unit></length>
}

object TrackStatus {
  def apply(e : Elem) : TrackStatus = 
    TrackStatus(
        online = (e \ "online").text == "true",
        name = (e \ "name").text,
        location = (e \ "location").text,
        tid = (e \ "tid").text.toLong,
        length = Length(e \ "length")
    )
}

case class TrackStatus(
    online : Boolean, 
    name : String, 
    location : String, 
    tid : Long, 
    length : Length
) {
  def toXml = <trackStatus>
      <online>{online}</online>
      <name>{name}</name>
      <location>{location}</location>
      <tid>{tid}</tid>
      {length.toXml}
    </trackStatus>
}

case class TrackPracticeDay(track : TrackStatus, practiceSessions : List[PracticeSessionListItem] = Nil) {
  lazy val sessionsNewestFirst = practiceSessions.sortWith((t1, t2)=>t1.date.after(t2.date))
}