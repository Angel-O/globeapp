package models.message

import models.common._

sealed trait MessageType

object MessageType {
  case class ClientConnected(userId: String) extends MessageType
  case class ClientDisconnected(userId: String, reason: String = "Log out") extends MessageType
  case object UserAuthenticated extends MessageType
  case class UserMessage(sender: String, content: String, recipient: String, _id: Option[String] = None) extends Entity with MessageType
  case class Notification(recipientId: String, senderId: String) extends MessageType
  
  import MessageTypeFormat._
  implicit val messageTypeFormat = MessageTypeFormat 
}
