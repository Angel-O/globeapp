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
import akka.stream.scaladsl.Sink

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global._
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow

class MessagingActor(val out: ActorRef) extends Actor{
  def receive = {
    case msg: Message => {
      Logger.info(s"Sending msg: $msg")
      (for {
        recipientId <- msg.receiver.userId
        senderId <- msg.sender.userId
      } yield Notification(senderId = senderId, recipientId = recipientId))
      .map(notification => out ! notification) 
      }
  }
}
case object MessagingActor {
  def props(out: ActorRef) = Props(classOf[MessagingActor], out)
}

class UserManagerActor() extends Actor {
  import UserManagerActor._
  var users: Map[String, ActorRef] = Map.empty
  
  def receive = {
    case ClientConnected(out, userId, msg) => {
      users = users + (userId -> context.actorOf(Props(new MessagingActor(out))))
      Logger.info(s"all users: $users")
      self ! MessageReceived(msg)
    }
    case MessageReceived(msg) => msg.receiver.userId flatMap users.get map { user => user ! msg }
  }
}

object UserManagerActor{
  def props() = Props[UserManagerActor]
  case class ClientConnected(out: ActorRef, userId: String, msg: Message)
  case class MessageReceived(msg: Message)
}

class MessageServerActor(val out: ActorRef, val userManager: ActorRef) extends Actor{
  import UserManagerActor._
  def receive = {
    case msg @ Message(sender, _, _, _, _) => userManager ! ClientConnected(out, sender.userId.get, msg)
  }
}
object MessageServerActor{
  def props(out: ActorRef, userManager: ActorRef) = Props(classOf[MessageServerActor], out, userManager)
}

@Singleton
class HomeController @Inject() (cc: ControllerComponents)(
  implicit
  system: ActorSystem,
  mat:    Materializer)
  extends AbstractController(cc) {
  
  implicit val messageFlowTransformer = jsonMessageFlowTransformer[Message, Notification]
    
  import UserManagerActor._
  
  val userManager = system.actorOf(Props[UserManagerActor], "userManager")
  
  // TODO send Notification object with recipient id and count of unread messages!!!
  // then to get the messages the client will have to perform ajax call
  def socket = WebSocket.accept[Message, Notification] { implicit request: RequestHeader => 
    Logger.info(s"Establishing connection...request id: ${request.id}")
    
    ActorFlow.actorRef(out => MessageServerActor.props(out, userManager))
    
//        val notifier = Flow[Message]
//    .map(msg =>
//      Notification(senderId = 
//        msg.sender.userId.get, 
//        recipientId = msg.receiver.userId.get))
//        
//    notifier
    
    // actor ref => message => notification
    
//    val messagesFlow = Flow[Message]
//    
//    val connectedFlow = Source
//    .actorRef[Message](1, OverflowStrategy.fail)
//    .map(msg => {userManager ! msg; msg})
//    
//    val source = connectedFlow via messagesFlow
//    
//    val notifier = source.mapMaterializedValue(actor =>
//      actor ! "hi")
    
    //val flow = connectedFlow via notifier
    
    //notifier to actorSource
    
    //val sink = Sink.foreach[UserManagerActor](x => x ! )
    //notifier
    //Flow.fromSinkAndSource(notifier, connectedFlow)
    
    //ActorFlow.actorRef(MessagingActor.props)
    //val in = Sink.foreach{ (x: Message) => Logger.info(x.content) }
    //val out = Source.actorRef(1, OverflowStrategy.dropNew)
    
    //Flow.fromSinkAndSource(in, out)
    
    //ActorFlow.actorRef(out => UserManagerActor.props(out))//.mapMaterializedValue(???)
    
    // Log events to the console
    // Notification(senderId = msg.sender.userId.get, recipientId = msg.receiver.userId.get)
  //val in = Sink.foreach[Message](msg => actors = actors + (msg.receiver.userId.get -> null))
   
    
//   val example = Flow[String]
//    .map(s ⇒ ByteString(s + "\n"))
//    .toMat(FileIO.toPath(Paths.get("")))(Keep.right)
    
//  val push = Flow[Notification]//.joinMat(ActorFlow.actorRef(MessagingActor.props))
//    .map(x => println(x))(Keep.right)
  
//  val sink = Flow[Message]
//    .map(msg => Notification(senderId = msg.sender.userId.get, recipientId = msg.receiver.userId.get))
//    .toMat(???)(Keep.right)
//  
//  val in = Sink.foreach[Message](msg => Notification(senderId = msg.sender.userId.get, recipientId = msg.receiver.userId.get))
//
//  // Send a single 'Hello!' message and then leave the socket open
//  val out = Source.maybe[Notification].map(x => {println(x); x})//.runForeach(println)//.concat(Source.maybe) //single(println).concat(Source.maybe)
//
//  Flow.fromSinkAndSourceMat(in, out)((i, o) => println)
  
  ////
  
//  def run(actorRef: ActorRef) = {
//     import scala.concurrent.ExecutionContext.Implicits.global
//     Future{ actorRef ! new Notification("", "") }
//  }
    
//  // a source of strings backed by an actor (every message sent to the actor will power the sink) 
//  val source = Source.actorRef[String](bufferSize = 0, OverflowStrategy.fail)
//  .mapMaterializedValue( actor => run(actor) )
//  
//  val source2 = Source.repeat(new Notification("hello", "world"))
//  
//  // a sink for strings: process each string by emitting the msg below
//  val sink = Sink.foreach[String](greet => println(s"Stuff happened: $greet"))
//  
//  val sink2 = Sink
//  .foreach[Message](msg => Notification(senderId = msg.sender.userId.get, recipientId = msg.receiver.userId.get))
//  //.mapMaterializedValue(_ => ???)
//  
//  // connects source and sink into a runnable flow
//  val flow = source to sink
//  
//  // evaluates(run, materializes) the flow
//  flow.run()
//  
//  val ok = ActorFlow.actorRef(out => MessagingActor.props(out))
//  
//  Flow.fromSinkAndSource(sink2, source2)
//  val flow2 = source2 to sink2
//  //flow2
//  //ok
    /////////// Akka.system.scheduler.schedule
  

    
    
    
    /////GOOD
//    val notifier = Flow[Message]
//    .map(msg =>
//      Notification(senderId = 
//        msg.sender.userId.get, 
//        recipientId = msg.receiver.userId.get))
//        
//    notifier
  }
}

//    case SaveUserActor(userId, actorRef) => if (!userActors.contains(userId)) { 
//      Logger.info(s"adding $userId"); userActors = userActors + (userId -> actorRef) }
//    case msg: Message => {
////      def run(actor: ActorRef) = {
////        import scala.concurrent.ExecutionContext.Implicits.global
////        val future = Future { self ! 1 }
////        future
////      }
////      val s = Source
////      .actorRef[Int](bufferSize = 0, OverflowStrategy.fail)
////      .mapMaterializedValue[Future[Unit]](run)
//      self ! SaveUserActor(msg.sender.userId.get, out)
//      Logger.info(s"Sending msg: $msg")
//      (for {
//        recipientId <- msg.receiver.userId
//        senderId <- msg.sender.userId
//      } yield Notification(senderId = senderId, recipientId = recipientId))
//      .map(notification => userActors.get(msg.receiver.userId.get).map(a => a ! notification)) 
//      //userActors(msg.sender.userId.get) ! msg
//    }
