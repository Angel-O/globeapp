package controllers

import actors.MessageServerActor
import actors.UserManagerActor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.Materializer
import apimodels.message.WsMessage
import apimodels.message.WsMessage.messageFormat
import apimodels.message.MessageType._
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.mvc.RequestHeader
import play.api.mvc.WebSocket
import play.api.mvc.WebSocket.MessageFlowTransformer.jsonMessageFlowTransformer
import akka.stream.scaladsl.Flow
import apimodels.message.MessageType

@Singleton
class HomeController @Inject() (cc: ControllerComponents)(
  implicit
  system: ActorSystem,
  mat:    Materializer)
  extends AbstractController(cc) {
  
  implicit val messageFlowTransformer = jsonMessageFlowTransformer[WsMessage, MessageType]
    
  val userFactory = system.actorOf(Props[UserManagerActor], "userFactory")
  
  def socket = WebSocket.accept[WsMessage, MessageType] { implicit request: RequestHeader => 
    
    Logger.info(s"Establishing connection...request id: ${request.id}")
    
    ActorFlow.actorRef(out => MessageServerActor.props(out, userFactory))
  }
}
