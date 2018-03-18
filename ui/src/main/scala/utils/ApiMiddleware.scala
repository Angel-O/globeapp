package utils

import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.window
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.ext.AjaxException
import appstate.{AppModel, AppCircuit, Connect}
import config._
import scala.language.implicitConversions 
import play.api.libs.json.Json._
import play.api.libs.json.Writes
import play.api.libs.json.Reads
import play.api.libs.json.JsValue

object ApiMiddleware {

  implicit def write[T](model: T)(implicit writes: Writes[T]) = {
    stringify(toJson(model))
  }

  implicit def read[T](json: JsValue)(implicit reads: Reads[T]) = {
    reads.reads(parse(json)).get //TODO make it safe if needed
  }

  implicit def toJsonString(resonseText: String) = {
    parse(resonseText)
  }
  
  private def token: String =
    window.sessionStorage.getItem(AUTHORIZATION_HEADER_NAME)
  val headers: Map[String, String] = Map.empty

  def getErrorCode(t: Throwable) = t match {
    case ex: AjaxException => ex.xhr.status
    case _                 => 0 //using zero to signify uknown response
  }

  def getStatusCode(xhr: XMLHttpRequest) = xhr.status

  def Get(url: String) = {
    Ajax.get(url = url,
             data = null,
             timeout = REQUEST_TIMEOUT,
             headers = setHeader((AUTHORIZATION_HEADER_NAME -> token)),
             withCredentials = false,
             responseType = RESPONSE_TYPE)
  }

  def Post(url: String,
           payload: Ajax.InputData,
           contentHeader: (String, String) = JSON_CONTENT_HEADER) = {
    Ajax.post(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader(
        (AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE
    )
  }

  def Delete(url: String,
             payload: Ajax.InputData,
             contentHeader: (String, String) = JSON_CONTENT_HEADER) = {
    Ajax.delete(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader(
        (AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE
    )
  }

  def Put(url: String,
          payload: Ajax.InputData,
          contentHeader: (String, String) = JSON_CONTENT_HEADER) = {
    Ajax.put(
      url = url,
      data = payload,
      timeout = REQUEST_TIMEOUT,
      headers = setHeader(contentHeader) ++ setHeader(
        (AUTHORIZATION_HEADER_NAME -> token)),
      withCredentials = false,
      responseType = RESPONSE_TYPE
    )
  }

  private def setHeader = (header: (String, String)) => {
    header._2 match {
      case "" | null => headers
      case _         => headers + header
    }
  }
}
