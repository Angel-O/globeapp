package utils

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.window
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.ext.AjaxException
import appstate.{AppModel, AppCircuit, Connect}
import config._

object ApiMiddleware { 
  private def token: String = window.sessionStorage.getItem(AUTHORIZATION_HEADER_NAME)
  val contentHeader = ("Content-type" -> "application/json")
  val headers: Map[String, String] = Map.empty

  
  def getStatusCode(t: Throwable) = t match {
    case ex: AjaxException => ex.xhr.status
    case _ => 0 //using zero to signify uknown response
  }

  def Get(url: String) = {
    Ajax.get(url = url,
             data = null,
             timeout = REQUEST_TIMEOUT,
             headers = setHeader((AUTHORIZATION_HEADER_NAME -> token)),
             withCredentials = false,
             responseType = RESPONSE_TYPE)
  }

  def Post(url: String, payload: Ajax.InputData) = {
    Ajax.post(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader((AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE)
  }

  def Delete(url: String, payload: Ajax.InputData) = {
    Ajax.delete(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader((AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE)
  }

  def Put(url: String, payload: Ajax.InputData) = {
    Ajax.put(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader((AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE)
  }

  private def setHeader = (header: (String, String)) => {
    header._2 match {
      case "" | null => headers
      case _         => headers + header
    }
  }
}