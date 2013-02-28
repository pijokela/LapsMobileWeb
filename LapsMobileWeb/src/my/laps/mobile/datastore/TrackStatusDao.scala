package my.laps.mobile.datastore

import my.laps.mobile.practice1.PracticeWebsiteDao
import my.laps.mobile.TrackStatus
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
import NiceEntity._


object TrackStatusDao {
  def trackStatusToEntity(trackStatus : TrackStatus) : Entity = {
    val itemKey = KeyFactory.createKey("TrackId", trackStatus.tid)
    val itemEntity = new Entity("TrackStatus", itemKey)
    TrackStatusDao.trackStatusToEntity(trackStatus, itemEntity)
    itemEntity
  }
  
  def trackStatusToEntity(trackStatus : TrackStatus, e : Entity) : Entity = {
    e.setProperty("tid", trackStatus.tid)
    e.xml(trackStatus.toXml)
    e
  }

  def isTid(trackStatusEntity : Entity, trackStatus : Long) : Boolean = true
}

class TrackStatusDao(websiteDao : PracticeWebsiteDao) {

  val datastore = DatastoreServiceFactory.getDatastoreService()

  private def findFromDatastore(tid : Long) = {
    val idIs = new FilterPredicate("tid", FilterOperator.EQUAL, tid)

    val trackKey = KeyFactory.createKey("TrackId", tid)
    val query = new Query("TrackStatus", trackKey).setFilter(idIs)
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10)).toList
    
    items.headOption
  }
  
  def storeOrUpdate(trackStatus : TrackStatus) : Unit = {
    val entity = findFromDatastore(trackStatus.tid)
    entity match {
      case Some(e) => 
        println("Updating track status: " + trackStatus.tid)
        datastore.put(TrackStatusDao.trackStatusToEntity(trackStatus, e))
      case None =>
        println("Creating new track status: " + trackStatus.tid)
        datastore.put(TrackStatusDao.trackStatusToEntity(trackStatus))
    }
  }
  
  def getTrackStatus(tid : Long) : TrackStatus = {
    val entity = findFromDatastore(tid)
    entity match {
      case Some(e) => TrackStatus(e.xml)
      case None => websiteDao.getTrackStatus(tid)
    }
  }
  
  def getRecentTracks() : List[TrackStatus] = {
    val query = new Query("TrackStatus")
    val items = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(200)).toList
    val tracks = items.map(e=>TrackStatus(e.xml))
    tracks.sortWith((s1, s2)=>s1.name.compareTo(s2.name) <= 0)
  }
}