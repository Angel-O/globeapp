package utils

import org.scalajs.dom.WebSocket
import org.scalajs.dom.Event
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.ErrorEvent
import api._
import json._
import config._
import play.api.libs.json.JsResult
import play.api.libs.json.Reads
import play.api.libs.json.Writes

//TODO rename to client
object WsMiddleware {
  val url = WEB_SOCKET_SERVER_ENDPOINT
  val socket = new WebSocket(url)

  class WsClient[In, Out](on: In => Unit)(implicit deserializer: Reads[In]) {

    socket.onopen = (_: Event) => println("ui socket connected")
    socket.onclose = (_: Event) => println("ui socket disconnected")
    socket.onerror = (_: ErrorEvent) => ()
    socket.onmessage = (me: MessageEvent) => read(me.data.toString) collect { case data => on(data) }


    def read[In](data: String)(implicit deserializer: Reads[In]) = readOpt[In](toJsonValue(data))
    def send[Out](msg: Out)(implicit serializer: Writes[Out]) = socket.readyState match {
      case 0 => ()
      case 1 => socket.send(write(msg)) //TODO handle other states properly and add many missing state
      case 2 => ()
      case _ => ()
    }
  }
}