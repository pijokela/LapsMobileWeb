package my.laps.mobile.practice1

import scala.io.Source
import scala.io.Codec
import my.laps.mobileweb.MylapsConf
import my.laps.mobileweb.MylapsConf
import my.laps.mobileweb.MylapsConf
import my.laps.mobile.LapValidator
import my.laps.mobile.PracticeSessionDay
import my.laps.mobile.TrackPracticeDay
import my.laps.mobile.TrackStatus
import scala.io.Codec.charset2codec

class PracticeWebsiteDao(urlBase : String, conf : MylapsConf = new MylapsConf()) {
  private def url(tid : Long) = urlBase + "/practice/showTrack.jsp?tid=" + tid

  private def sourceFromUrlString(url : String) = Source.fromURL(url)(Codec.UTF8)
  
  def getTrackStatus(tid : Long) : TrackStatus = {
    val urlString = url(tid)
    val source = sourceFromUrlString(urlString)
    val parser = try {
      new TrackStatusParser(source, conf)
    }
    catch {
      case e : Exception => throw new RuntimeException("Failed to load web page from: " + urlString, e)
    }
    finally {
      source.close
    }
    parser.parseTrackStatus
  }

  def getTrackPracticeDay(tid : Long) : TrackPracticeDay = {
    val urlString = url(tid)
    val source = sourceFromUrlString(urlString)
    val parser = try {
      new TrackStatusParser(source, conf)
    }
    catch {
      case e : Exception => throw new RuntimeException("Failed to load web page from: " + urlString, e)
    }
    finally {
      source.close
    }
    parser.parseTrackPracticeDay
  }
  
  private def url(tid : Long, transponder : Long) = urlBase + "/practice/showLaptimes.jsp?tid=" + tid + "&transponder=" + transponder
  
  def getTransponderSessions(tid : Long, transponder : Long, validator : LapValidator) : PracticeSessionDay = {
    val urlString = url(tid, transponder)
    println("Loading sessions from url: " + urlString)
    val source = sourceFromUrlString(urlString)
    val track = getTrackStatus(tid)
    println("Found track: " + track)
    val parser = new PracticeSessionParser(track, source, validator, conf)
    source.close
    println("Closed source.")
    parser.parseTrackPracticeDay
  }
  
}