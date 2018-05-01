package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import apimodels.message.WsMessage
import apimodels.message.MessageType
import apimodels.message.MessageType._
import play.api.Logger
import actors.MessageServerActor.CreateConnection

class UserManagerActor() extends Actor {
  import UserManagerActor._
  private var users: Map[String, ActorRef] = Map.empty

  def receive = {
    case CreateConnection(userId, out) => {
      users = users + (userId -> context.actorOf { MessagingActor.props(out) })
      Logger.info(s"New Connection established. ALL CONNECTIONS: $users")
    }
    case ClientDisconnected(userId, reason) => {
      users = users - userId
      Logger.info(s"Connection removed reason ($reason). ALL CONNECTIONS: : $users")
    }
    case MessageReceived(msgType, senderId) => msgType match {
      case msg @ UserMessage(_, recipientId) => sendNotification(senderId, recipientId, msg)
    }
  }

  private def sendNotification(senderId: String, recipientId: String, msg: UserMessage) = {
    Logger.info(s"Sending notification (msg = $msg)")
    users get recipientId map { user => user ! SendNotification(msg, senderId) }
  }
}

object UserManagerActor {
  def props() = Props[UserManagerActor]
  case class MessageReceived(msg: MessageType, sender: String)
  case class SendNotification(userMessage: UserMessage, senderId: String)
}