package my.laps.mobile.datastore

import my.laps.mobile.practice1.PracticeWebsiteDao
import my.laps.mobile.TrackPracticeDay
import my.laps.mobile.Driver
import my.laps.mobile.Transponder
import my.laps.mobile.practice1.AllLapsFromUserOnTrackDao
import my.laps.mobile.TrackStatus
import my.laps.mobile.Day
import my.laps.mobile.PracticeSessionDay
import my.laps.mobile.LapValidator
import my.laps.mobile.TrackPracticeDay
import my.laps.mobile.PracticeSessionListItem
import NiceEntity._
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Text
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query.FilterPredicate
import com.google.appengine.api.datastore.Query.FilterOperator
import com.google.appengine.api.datastore.Query.CompositeFilterOperator
import com.google.appengine.api.datastore.Query.CompositeFilter
import scala.xml.XML
import scala.xml.Elem
import java.util.Arrays
import java.util.Date


object PracticeDatastoreDao {
  
  /**
   * Update entity from item. The parameter is modified and returned.
   */
  def practiceSessionListItemToEntity(tid : Long, item : PracticeSessionListItem, itemEntity : Entity) : Entity = {
    itemEntity.setProperty("id", item.id)
    itemEntity.setProperty("tid", tid)
    itemEntity.setProperty("date", item.date)
    itemEntity.xml(item.toXml)
    itemEntity
  }
  
  /**
   * Create entity from item.
   */
  def practiceSessionListItemToEntity(tid : Long, item : PracticeSessionListItem) : Entity = {
    val itemKey = KeyFactory.createKey("TrackId", tid)
    val itemEntity = new Entity("PracticeSessionListItem", itemKey)
    PracticeDatastoreDao.practiceSessionListItemToEntity(tid, item, itemEntity)
    itemEntity
  }

  private def isOnDay(e : Entity, day : Day) : Boolean = 
    Day(e.xml\\"day") == day
  
  def entityToPracticeSessionListItem(e : Entity) : (Long, PracticeSessionListItem) = 
    (
        e.longProp("tid"),
        PracticeSessionListItem(e.xml)
    )
    
  /**
   * Update entity from item. The parameter is modified and returned.
   */
  def practiceSessionDayToEntity(session : PracticeSessionDay, itemEntity : Entity) : Entity = {
    itemEntity.setProperty("id", session.id)
    itemEntity.setProperty("tid", session.track.tid)
    itemEntity.setProperty("driver.transponder.number", session.driver.transponder.number)
    itemEntity.xml(session.toXml)
    itemEntity
  }
  
  /**
   * Create entity from item.
   */
  def practiceSessionDayToEntity(session : PracticeSessionDay) : Entity = {
    val itemKey = KeyFactory.createKey("TrackId", session.track.tid)
    val itemEntity = new Entity("PracticeSessionDay", itemKey)
    PracticeDatastoreDao.practiceSessionDayToEntity(session, itemEntity)
    itemEntity
  }

  def entityToPracticeSessionDay(e : Entity) : (Long, PracticeSessionDay) = 
    (
        e.longProp("tid"),
        PracticeSessionDay(e.xml)
    )
    
}

/**
 * Dao class contains interface methods for the servlets.
 */
class PracticeDatastoreDao(websiteDao : PracticeWebsiteDao, trackStatusDao : TrackStatusDao, allLapsWebsiteDao : AllLapsFromUserOnTrackDao) {
  
  val datastore = DatastoreServiceFactory.getDatastoreService()
  
  def getTrackPracticeDay(tid : Long) : TrackPracticeDay = {
    // Get new results from the web page:
    val newResults = websiteDao.getTrackPracticeDay(tid)
    
    // Store new results:
    newResults.practiceSessions.foreach(i=>storeOrUpdate(tid, i))
    
    // Read combined results:
    val trackKey = KeyFactory.createKey("TrackId", tid)
    val tidIs = new FilterPredicate("tid", FilterOperator.EQUAL, tid);
    val query = new Query("PracticeSessionListItem", trackKey).setFilter(tidIs).addSort("date", Query.SortDirection.DESCENDING)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(30)).toList
    val items2 = items.map(e=>PracticeDatastoreDao.entityToPracticeSessionListItem(e)._2)
    
    println("Reading track activity: " + newResults.practiceSessions.size + " from web " + items.size + " from DB " + items2.size + " for RSS")
    println(items2)
    // Return combined results:
    TrackPracticeDay(newResults.track, items2)
  }
  
  def getTransponderSessions(tid : Long, 
                             transponder : Long, 
                             day : Option[Day], 
                             validator : LapValidator) : PracticeSessionDay = 
  {
    
    day match {
      case None => 
        // When results are for today, always update from practice website:
        val practiceDay = websiteDao.getTransponderSessions(tid, transponder, validator)
        storeOrUpdate(practiceDay)
        practiceDay
      case Some(day) => 
        findPracticeDay(tid, transponder, day).orElse {
            println("Looking for more results for day " + day)
            // When results are older, update if data is missing:
            val practiceDay = websiteDao.getTransponderSessions(tid, transponder, validator)
            println(" -- driver: " + practiceDay.driver)
            println(" -- track: " + practiceDay.track)
            updateData(practiceDay.track, practiceDay.driver)
            findPracticeDay(tid, transponder, day)
        }.getOrElse(
            throw new IllegalArgumentException("No results for " + transponder + " on day " + day.toString)
        )
    }
  }
  
  /**
   * Reads all data from practice1 for this transponder and track and stores it to db.
   */
  def updateData(track : TrackStatus, driver : Driver) {
    val sessionDays = allLapsWebsiteDao.getAllPracticeSessionDaysForDriver(track, driver)
    println("Found " + sessionDays.size + " days for driver: " + driver)
    sessionDays.map(storeOrUpdate(_))
    sessionDays.map(_.toPracticeSessionListItem).map(storeOrUpdate(track.tid, _))
  }
  
  def findPracticeDay(tid : Long, transponder : Long, day : Day) : Option[PracticeSessionDay] = {
    val trackKey = KeyFactory.createKey("TrackId", tid)
    val tidIs = new FilterPredicate("tid", FilterOperator.EQUAL, tid)
    val transponderIs = new FilterPredicate("driver.transponder.number", FilterOperator.EQUAL, transponder);
    val filter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(tidIs, transponderIs))
    val query = new Query("PracticeSessionDay", trackKey).setFilter(filter)//.addSort("day", Query.SortDirection.ASCENDING)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000)).toList
    val practiceSesssionDaoOption = 
      items.find(i=>Day(i.xml\\"day") == day).map(i=>PracticeDatastoreDao.entityToPracticeSessionDay(i)._2)
    // Since the session day is coming from datastore, let's update the 
    // track data from the newest status we have for that track:
    practiceSesssionDaoOption.map(_.copy(track = trackStatusDao.getTrackStatus(tid)))
  }
  
  
  private def storeOrUpdate(session : PracticeSessionDay) : Unit = {
    
    val idIs = new FilterPredicate("id", FilterOperator.EQUAL, session.id)

    val trackKey = KeyFactory.createKey("TrackId", session.track.tid)
    val query = new Query("PracticeSessionDay", trackKey).setFilter(idIs)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000)).toList
    
    val entity = items.find(i=>PracticeDatastoreDao.isOnDay(i, session.day))
    entity match {
      case Some(e) => 
        println("Updating session day entity: " + session.day)
        datastore.put(PracticeDatastoreDao.practiceSessionDayToEntity(session, e))
      case None =>
        println("Creating new session day entity: " + session.day)
        datastore.put(PracticeDatastoreDao.practiceSessionDayToEntity(session))
    }
    
    // Store track to datastore:
    trackStatusDao.storeOrUpdate(session.track)
  }
  
  
  private def storeOrUpdate(tid : Long, item : PracticeSessionListItem) : Unit = {
    
    val idIs = new FilterPredicate("id", FilterOperator.EQUAL, item.id)

    val trackKey = KeyFactory.createKey("TrackId", tid)
    val query = new Query("PracticeSessionListItem", trackKey).setFilter(idIs)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100)).toList
    
    val entity = items.find(i=>PracticeDatastoreDao.isOnDay(i, item.sessionDate))
    entity match {
      case Some(e) => 
        datastore.put(PracticeDatastoreDao.practiceSessionListItemToEntity(tid, item, e))
      case None =>
        datastore.put(PracticeDatastoreDao.practiceSessionListItemToEntity(tid, item))
    }
  }
  
}

