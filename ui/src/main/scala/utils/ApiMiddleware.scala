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
import JsonMiddleware.toJsonValue
import JwtMiddleware.{getToken => token}

object ApiMiddleware {

  implicit def write[T](model: T)(implicit serializer: Writes[T]) = {
    // equivalent to stringify(toJson(model))
    stringify(serializer.writes(model))
  }

  implicit def read[T](json: JsValue)(implicit deserializer: Reads[T]) = {
    // equivalent to reads.reads(parse(json)).get
    deserializer.reads(parse(json)).get //TODO make it safe if needed
  }
  
  implicit def readOpt[T](json: JsValue)(implicit deserializer: Reads[T]) = {
    // equivalent to reads.reads(parse(json)).get
    parse(json).validateOpt.get
    //deserializer.reads(dd) //TODO make it safe if needed
  }
    
  val headers: Map[String, String] = Map.empty

  def getErrorCode(t: Throwable) = t match {
    case ex: AjaxException => ex.xhr.status
    case _                 => 0 //using zero to signify unknown response
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
             payload: Ajax.InputData = null,
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
          payload: Ajax.InputData = null,
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
