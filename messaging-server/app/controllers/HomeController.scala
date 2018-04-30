package controllers

import actors.MessageServerActor
import actors.UserManagerActor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.Materializer
import apimodels.common.Notification
import apimodels.common.Notification.format
import apimodels.message.Message
import apimodels.message.Message.messageFormat
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

@Singleton
class HomeController @Inject() (cc: ControllerComponents)(
  implicit
  system: ActorSystem,
  mat:    Materializer)
  extends AbstractController(cc) {
  
  implicit val messageFlowTransformer = jsonMessageFlowTransformer[Message, Notification]
    
  val userFactory = system.actorOf(Props[UserManagerActor], "userFactory")
  
  def socket = WebSocket.accept[Message, Notification] { implicit request: RequestHeader => 
    
    Logger.info(s"Establishing connection...request id: ${request.id}")
    
    ActorFlow.actorRef(out => MessageServerActor.props(out, userFactory))
  }
  
//  def connect = WebSocket.accept[String, String] { implicit request: RequestHeader =>
//    Flow[String].map(_ => "connection accepted")
//  }
}
