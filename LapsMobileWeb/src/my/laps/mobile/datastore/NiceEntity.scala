package my.laps.mobile.datastore

import com.google.appengine.api.datastore._
import scala.xml.Elem
import scala.xml.XML
import java.util.Date


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
  def xml(xml : Elem) = {
    val text = new Text(xml.toString)
    entity.setProperty("xml", text)
  }
  def xml() : Elem = {
    val text = entity.getProperty("xml").asInstanceOf[Text]
    XML.loadString(text.getValue())
  }
  def strProp(name : String) = entity.getProperty(name).asInstanceOf[String]
  def longProp(name : String) = entity.getProperty(name).asInstanceOf[Long]
  def intProp(name : String) = entity.getProperty(name).asInstanceOf[Long].toInt
  def dateProp(name : String) = entity.getProperty(name).asInstanceOf[Date]
}
