package actors

import actors.UserManagerActor.SendNotification
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import apimodels.message.MessageType._
import play.api.Logger

class MessagingActor(val out: ActorRef) extends Actor {
  def receive = {
    case SendNotification(
      msg: UserMessage, //TODO what to do with the content ??
      senderId) => {

      out ! Notification(senderId = senderId, recipientId = msg.recipient)
      Logger.info(s"Message sent: $msg")
    }
  }
}
case object MessagingActor {
  def props(out: ActorRef) = Props(classOf[MessagingActor], out)
}