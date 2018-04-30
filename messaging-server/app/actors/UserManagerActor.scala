package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import apimodels.message.Message
import play.api.Logger

class UserManagerActor() extends Actor {
  import UserManagerActor._
  var users: Map[String, ActorRef] = Map.empty
  
  def receive = {
    case ClientConnected(out, userId, msg) => {
      users = users + (userId -> context.actorOf{ Props { new MessagingActor(out) }})
      Logger.info(s"all users: $users")
      self ! MessageReceived(msg)
    }
    case MessageReceived(msg) => msg.recipient.userId flatMap users.get map { user => user ! msg }
  }
}

object UserManagerActor{
  def props() = Props[UserManagerActor]
  case class ClientConnected(out: ActorRef, userId: String, msg: Message)
  case class MessageReceived(msg: Message)
}