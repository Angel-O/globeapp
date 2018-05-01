package actors

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import apimodels.message.WsMessage
import apimodels.message.MessageType._
import UserManagerActor._
import play.api.Logger

class MessageServerActor(val out: ActorRef, val userManager: ActorRef) extends Actor {
  import MessageServerActor._

  def receive = {
    case WsMessage(sender, messageType, _, _) => messageType match {
      case msg: ClientDisconnected => userManager ! msg
      case ClientConnected(userId) => userManager ! CreateConnection(userId, out)
      case msg: UserMessage        => userManager ! MessageReceived(msg, sender)
      case _                       => println("MMMMMM", messageType)
    }
  }
}

object MessageServerActor {
  case class CreateConnection(userId: String, out: ActorRef)
  def props(out: ActorRef, userManager: ActorRef) = Props(classOf[MessageServerActor], out, userManager)
}