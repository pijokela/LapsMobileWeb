package my.laps.mobileweb

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import my.laps.mobile.Day

/**
 * A class that disconnects the rest of the code from javax.servlet api.
 */
class MyHttpRequest(
    val getParameter : (String)=>String,
    val getCookies : ()=>Array[Cookie]
)

/**
 * A class that disconnects the rest of the code from javax.servlet api.
 */
class MyHttpResponse(
    val addCookie: (Cookie)=>Unit
)

object HttpServletRequestParsing {
  implicit def toMyRequest(r : HttpServletRequest) = 
    new MyHttpRequest(r.getParameter, r.getCookies)
  implicit def toMyResponse(r : HttpServletResponse) = 
    new MyHttpResponse(r.addCookie)
}

trait HttpServletRequestParsing {
  def paramOption(name : String, req : MyHttpRequest) : Option[String] = 
    if (req.getParameter(name) == null) None else Some(req.getParameter(name))
  
  def longParamOption(name : String, req : MyHttpRequest) : Option[Long] = 
    try {
      paramOption(name, req).map(_.toLong)
    }
    catch {
      case t : NumberFormatException => None
    }
    
  def dayParamOption(name : String, req : MyHttpRequest) : Option[Day] = 
    try {
      val dayString = paramOption(name, req).getOrElse(return None)
      val parts = dayString.split("-").map(_.toInt)
      Some(Day(parts(0), parts(1), parts(2)))
    }
    catch {
      case t : NumberFormatException => None
      case t : ArrayIndexOutOfBoundsException => None
    }
    
  
  def cookieOption(name : String, req : MyHttpRequest) : Option[Cookie] = {
    val cookies = req.getCookies()
    if (cookies == null) return None
    cookies.find(_.getName() == name)
  }
  
  def cookieValueOption(name : String, req : MyHttpRequest) : Option[String] = 
    cookieOption(name, req).map(_.getValue)
  
  def paramValueOption(name : String, req : MyHttpRequest) : Option[String] =
    req.getParameter(name) match {
      case null => None
      case v => Some(v)
    }
  
  def getTidFromCookie(req : MyHttpRequest) =
    cookieOption("tid", req).map(_.getValue().toLong).getOrElse(1429L)

  def putTidToCookie(tid : Long, resp : MyHttpResponse) = 
	resp.addCookie(new Cookie("tid", tid.toString))
	
  def putCookie(name : String, value : String, resp : MyHttpResponse) = {
    val cookie = new Cookie(name, value)
    cookie.setMaxAge(3600*24*365)
    resp.addCookie(cookie)
  }
}