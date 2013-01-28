package my.laps.mobile.datastore

import my.laps.mobile.PracticeWebsiteDao
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
    val items2 = items.map(e=>entityToPracticeSessionListItem(e)._2)
    
    println("Reading track activity: " + newResults.practiceSessions.size + " from web " + items.size + " from DB " + items2.size + " for RSS")
    println(items2)
    // Return combined results:
    TrackPracticeDay(newResults.track, items2)
  }
  
  def storeOrUpdate(tid : Long, item : PracticeSessionListItem) : Unit = {
    
    val idIs = new FilterPredicate("id", FilterOperator.EQUAL, item.id)

    val trackKey = KeyFactory.createKey("TrackId", tid)
    val query = new Query("PracticeSessionListItem", trackKey).setFilter(idIs)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100)).toList
    
    val entity = items.find(i=>i.dateProp("date") == item.date)
    entity match {
      case Some(e) => 
        datastore.put(practiceSessionListItemToEntity(tid, item, e))
      case None =>
        datastore.put(practiceSessionListItemToEntity(tid, item))
    }
  }
  
  /**
   * Create entity from item.
   */
  def practiceSessionListItemToEntity(tid : Long, item : PracticeSessionListItem) : Entity = {
    val itemKey = KeyFactory.createKey("TrackId", tid)
    val itemEntity = new Entity("PracticeSessionListItem", itemKey)
    practiceSessionListItemToEntity(tid, item, itemEntity)
    itemEntity
  }

  /**
   * Update entity from item. The parameter is modified and returned.
   */
  def practiceSessionListItemToEntity(tid : Long, item : PracticeSessionListItem, itemEntity : Entity) : Entity = {
    itemEntity.setProperty("id", item.id)
    itemEntity.setProperty("tid", tid)
    itemEntity.setProperty("date", item.date)
    itemEntity.setProperty("sessionDate.year", item.sessionDate.year)
    itemEntity.setProperty("sessionDate.month", item.sessionDate.month)
    itemEntity.setProperty("sessionDate.day", item.sessionDate.day)
    itemEntity.setProperty("passings", item.passings)
    itemEntity.setProperty("driver.name", item.driver.name)
    itemEntity.setProperty("driver.transponder.number", item.driver.transponder.number)
    itemEntity
  }

  def entityToPracticeSessionListItem(e : Entity) : (Long, PracticeSessionListItem) = 
    (
        e.longProp("tid"),
        PracticeSessionListItem(
            Driver(
                Transponder(e.longProp("driver.transponder.number")), e.strProp("driver.name")
            ),
            e.dateProp("date"),
            Day(
                e.intProp("sessionDate.year"),
                e.intProp("sessionDate.month"),
                e.intProp("sessionDate.day")
            ),
            e.intProp("passings")
        )
    )
}
