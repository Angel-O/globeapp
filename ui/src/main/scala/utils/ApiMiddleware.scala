package utils

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.window
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.ext.AjaxException
import appstate.{AppModel, AppCircuit, Connect}

//TODO move some stuff to config
//Do not hardcode auth header name...read from config and use apply to set it
//to improve reusability
//def apply(string: authorizationHeaderName = "Authorization")
object ApiMiddleware { 
  private def token: String = window.sessionStorage.getItem("Token")
  val contentHeader = ("Content-type" -> "application/json")
  val headers: Map[String, String] = Map.empty

  
  def getStatusCode(t: Throwable) = t match {
    case ex: AjaxException => ex.xhr.status
    case _ => 0 //TODO using zero to signify uknown...is there a code for that already??
  }

  def Get(url: String) = {
    Ajax.get(url = url,
             data = null,
             timeout = 9000,
             headers = setHeader(("Token" -> token)),
             withCredentials = false,
             responseType = "text")
  }

  def Post(url: String, payload: Ajax.InputData) = {
    Ajax.post(
      url = url,
      data = payload,
      timeout = 9000,
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)),
      withCredentials = false,
      responseType = "text")
  }

  def Delete(url: String, payload: Ajax.InputData) = {
    Ajax.delete(
      url = url,
      data = payload,
      timeout = 9000,
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)),
      withCredentials = false,
      responseType = "text")
  }

  def Put(url: String, payload: Ajax.InputData) = {
    Ajax.put(
      url = url,
      data = payload,
      timeout = 9000,
      headers = setHeader(contentHeader) ++ setHeader(("Token" -> token)),
      withCredentials = false,
      responseType = "text")
  }

  private def setHeader = (header: (String, String)) => {
    header._2 match {
      case "" | null => headers
      case _         => headers + header
    }
  }
}