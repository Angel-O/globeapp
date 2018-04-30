package actors

import akka.actor.ActorRef
import akka.actor.Actor
import apimodels.message.Message
import play.api.Logger
import apimodels.common.Notification
import akka.actor.Props

class MessagingActor(val out: ActorRef) extends Actor{
  def receive = {
    case msg: Message => {
      Logger.info(s"Sending msg: $msg")
      (for {
        recipientId <- msg.recipient.userId
        senderId <- msg.sender.userId
      } yield Notification(senderId = senderId, recipientId = recipientId))
      .map(notification => out ! notification) 
      }
  }
}
case object MessagingActor {
  def props(out: ActorRef) = Props(classOf[MessagingActor], out)
}