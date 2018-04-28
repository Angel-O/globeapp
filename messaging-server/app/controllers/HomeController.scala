package controllers


import akka.actor._
import akka.stream._
import javax.inject._
import play.api.libs.streams._
import play.api.mvc._


case class MessagingActor(out: ActorRef) extends Actor{
  def receive = {
    case msg => out ! s"I received a msg: ($msg)"
  }
}
case object MessagingActor {
  def props(out: ActorRef) = Props(new MessagingActor(out))
}

@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, materializer: Materializer) 
extends AbstractController(cc) {
  
  def socket = WebSocket.accept[String, String] { request => 
    ActorFlow.actorRef(out => MessagingActor.props(out))
  }
}
