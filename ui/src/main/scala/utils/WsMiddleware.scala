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
import apimodels.message.WsMessage
import apimodels.message.MessageType
import apimodels.common.Author //Remove dependency on this
import scala.concurrent.Future

//TODO rename to client
object WsMiddleware {
  val url = WEB_SOCKET_SERVER_ENDPOINT

  class WsClient[T <: MessageType, S <: MessageType](
    //on:        MessageType => Unit,
    onConnect: => T,
    onDisconnect: => S,
    onClose:   () => Unit = () => (),
    url: String = WEB_SOCKET_SERVER_ENDPOINT)
    (implicit deserializer: Reads[WsMessage], serializer: Writes[WsMessage]) {
    
    private var socket: WebSocket = null //= new WebSocket(url)
    
    // restore the underlying socket if it's closed
    // to reopen the connection the the .ctor must be invoked so this
    // code will be executed on instantiation
    // if(socket.readyState == 3) socket = new WebSocket(url)

    private def configSocket = {
        socket.onopen = (_: Event) => {
        println("about to send")
        this.send(new WsMessage(
            sender = Author(userId = Some("CONN"), name = ""),
            messageType =  onConnect,
            dateCreated = None))
        println("MSG IS", write(onConnect)) //TODO userId is always empty...
        println("STATE",socket.readyState)
        println("ui socket connected")
      }
        
        socket.onclose = (_: Event) => {
  //      send(new WsMessage(
  //          sender = Author(userId = Some("DISCONN"), name = ""),
  //          messageType =  onDisconnect,
  //          dateCreated = None))
        onClose() //TODO improve this..
        println("ui socket disconnected")
      }
      socket.onerror = (e: ErrorEvent) => {
        println("socket error", e.message)
      }
    }
      
    
//    socket.onmessage = (me: MessageEvent) =>
//      read(me.data.toString) collect { case data => on(data) }

    def read(data: String) =
      readOpt[MessageType](toJsonValue(data))
      
    def disconnect = {
      import scala.concurrent.ExecutionContext.Implicits.global
      Future{ send(new WsMessage(
          sender = Author(userId = Some("DISCONN"), name = ""),
          messageType =  onDisconnect,
          dateCreated = None)) } map (_ => closeSocket(onDisconnect)) // Nah..
     }
    
    def reconnect = {
      println("reconnecting underlying socket")
      if(socket.readyState == 3) socket = new WebSocket(url)
    }
    
    def open = {
      println("creating underlying socket")
      if(socket == null || socket.readyState == 3) {
        socket = new WebSocket(url)
        configSocket
      }
    }
    
    def isOpen = {
      socket != null && socket.readyState == 1
    }
    
    def handleMessageWith(onMessage: MessageType => Unit) = {
      socket.onmessage = (me: MessageEvent) =>
      read(me.data.toString) collect { case data => onMessage(data) }
    }
    
    def handleCloseWith(handler: () => Unit) = {
      socket.onclose = (_: Event) => handler()
    }
    
    private def closeSocket(reason: String) = {
      socket.readyState match {
        case 1 => socket.close(reason = reason)
      }
    }

    def send(msg: WsMessage) =
      //TODO handle other states properly and add many missing state
      if (socket != null)
      socket.readyState match {
        case 0 => ()
        case 1 => socket.send(write(msg))
        case 2 => ()
        case _ => ()
      }
  }
}
