package my.laps.mobile.datastore

import my.laps.mobile.practice1.PracticeWebsiteDao
import my.laps.mobile.TrackPracticeDay
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import my.laps.mobile.PracticeSessionListItem
import com.google.appengine.api.datastore.KeyFactory
import my.laps.mobile.PracticeSessionListItem
import my.laps.mobile.PracticeSessionListItem
import my.laps.mobile.Driver
import my.laps.mobile.Transponder
import NiceEntity._
import java.util.Date
import my.laps.mobile.TrackPracticeDay
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.FetchOptions
import my.laps.mobile.PracticeSessionListItem
import com.google.appengine.api.datastore.Query.FilterPredicate
import com.google.appengine.api.datastore.Query.FilterOperator
import com.google.appengine.api.datastore.Query.CompositeFilterOperator
import my.laps.mobile.Day
import my.laps.mobile.PracticeSessionDay
import my.laps.mobile.LapValidator
import my.laps.mobile.PracticeSessionDay
import scala.xml.XML

/**
 * Convert google entity objects to nice entities.
 */
object NiceEntity {
  implicit def toNiceEntity(e : Entity) = new NiceEntity(e)
  implicit def toList[A](javaList : java.util.List[A]) : List[A] = javaList.toArray().map(_.asInstanceOf[A]).toList
}

/**
 * Create a nicer interface for handling entities here.
 */
class NiceEntity(entity : Entity) {
  def strProp(name : String) = entity.getProperty(name).asInstanceOf[String]
  def longProp(name : String) = entity.getProperty(name).asInstanceOf[Long]
  def intProp(name : String) = entity.getProperty(name).asInstanceOf[Long].toInt
  def dateProp(name : String) = entity.getProperty(name).asInstanceOf[Date]
}


object PracticeDatastoreDao {
  
  /**
   * Update entity from item. The parameter is modified and returned.
   */
  def practiceSessionListItemToEntity(tid : Long, item : PracticeSessionListItem, itemEntity : Entity) : Entity = {
    itemEntity.setProperty("id", item.id)
    itemEntity.setProperty("tid", tid)
    itemEntity.setProperty("date", item.date)
    itemEntity.setProperty("xml", item.toXml.toString)
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
    Day(e.intProp("sessionDate.year"), e.intProp("sessionDate.month"), e.intProp("sessionDate.day")) == day
  
  def entityToPracticeSessionListItem(e : Entity) : (Long, PracticeSessionListItem) = 
    (
        e.longProp("tid"),
        PracticeSessionListItem(
            XML.loadString(e.strProp("xml"))
        )
    )
    
  /**
   * Update entity from item. The parameter is modified and returned.
   */
  def practiceSessionDayToEntity(session : PracticeSessionDay, itemEntity : Entity) : Entity = {
    itemEntity.setProperty("id", session.id)
    itemEntity.setProperty("tid", session.track.tid)
    itemEntity.setProperty("driver.transponder.number", session.driver.transponder.number)
    itemEntity.setProperty("xml", session.toXml.toString)
    itemEntity
  }
  
  /**
   * Create entity from item.
   */
  def practiceSessionDayToEntity(session : PracticeSessionDay) : Entity = {
    val itemKey = KeyFactory.createKey("TrackId", session.track.tid)
    val itemEntity = new Entity("PracticeSessionListItem", itemKey)
    PracticeDatastoreDao.practiceSessionDayToEntity(session, itemEntity)
    itemEntity
  }

  def entityToPracticeSessionDay(e : Entity) : (Long, PracticeSessionDay) = 
    (
        e.longProp("tid"),
        PracticeSessionDay(
            XML.loadString(e.strProp("xml"))
        )
    )
    
}

/**
 * Dao class contains interface methods for the servlets.
 */
class PracticeDatastoreDao(websiteDao : PracticeWebsiteDao) {
  
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
  
  def getTransponderSessions(tid : Long, transponder : Long, day : Option[Day], validator : LapValidator) : PracticeSessionDay = {
    val practiceDay = websiteDao.getTransponderSessions(tid, transponder, validator)
    storeOrUpdate(practiceDay)
    
    practiceDay
  }
  
  
  private def storeOrUpdate(session : PracticeSessionDay) : Unit = {
    
    val idIs = new FilterPredicate("id", FilterOperator.EQUAL, session.id)

    val trackKey = KeyFactory.createKey("TrackId", session.track.tid)
    val query = new Query("PracticeSessionDay", trackKey).setFilter(idIs)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000)).toList
    
    val entity = items.find(i=>PracticeDatastoreDao.isOnDay(i, session.day))
    entity match {
      case Some(e) => 
        datastore.put(PracticeDatastoreDao.practiceSessionDayToEntity(session, e))
      case None =>
        datastore.put(PracticeDatastoreDao.practiceSessionDayToEntity(session))
    }
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

