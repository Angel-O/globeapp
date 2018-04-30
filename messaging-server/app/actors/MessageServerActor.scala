package actors

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import apimodels.message.Message
import UserManagerActor._
import play.api.Logger

class MessageServerActor(val out: ActorRef, val userManager: ActorRef) extends Actor{
  
  def receive = {
    case msg @ Message(sender, _, _, _, _) => sender.userId map { id => userManager ! ClientConnected(out, id, msg) }
    //case whatever @ _ => Logger.info(s"Whateva: ${whatever}")
  }
}

object MessageServerActor{
  def props(out: ActorRef, userManager: ActorRef) = Props(classOf[MessageServerActor], out, userManager)
}