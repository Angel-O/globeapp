package controllers


import akka.actor._
import akka.stream._
import javax.inject._
import play.api.libs.streams._
import play.api.mvc._
import play.api.Logger

import apimodels.message.Message
import apimodels.message.Message._
import apimodels.common.Notification
import apimodels.common.Notification._

import play.api.mvc._

import play.api.mvc.WebSocket.MessageFlowTransformer._
import play.api.mvc.WebSocket.MessageFlowTransformer


case class MessagingActor(out: ActorRef) extends Actor{
  def receive = {
    case msg: Message =>
      (for {
        recipientId <- msg.receiver.userId
        senderId <- msg.sender.userId
      } yield Notification(senderId = senderId, recipientId = recipientId))
      .map(notification => out ! notification)
  }
}
case object MessagingActor {
  def props(out: ActorRef) = Props(new MessagingActor(out))
}

@Singleton
class HomeController @Inject() (cc: ControllerComponents)(
  implicit
  system: ActorSystem,
  mat:    Materializer)
  extends AbstractController(cc) {
  
  implicit val messageFlowTransformer = jsonMessageFlowTransformer[Message, Notification]

  // TODO send Notification object with recipient id and count of unread messages!!!
  // then to get the messages the client will have to perform ajax call
  def socket = WebSocket.accept[Message, Notification] { implicit request => 
    Logger.info(s"Establishing connection...request id: ${request.id}")
    ActorFlow.actorRef(out => MessagingActor.props(out))
  }
}
