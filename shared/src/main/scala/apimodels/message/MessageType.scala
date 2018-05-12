package apimodels.message

import apimodels.common._

sealed trait MessageType

object MessageType {
  case class ClientConnected(userId: String) extends MessageType
  case class ClientDisconnected(userId: String, reason: String = "Log out") extends MessageType
  case object UserAuthenticated extends MessageType
  case class UserMessage(sender: String, content: String, recipient: String, _id: Option[String] = None) extends Entity with MessageType
  case class Notification(recipientId: String, senderId: String) extends MessageType
  case object CheckConnectionAlive extends MessageType
  case class ConnectionAlive(userId: String, connectionDate: String) extends MessageType //adding local date to differentiate from other messages
  
  import MessageTypeFormat._
  implicit val messageTypeFormat = MessageTypeFormat 
}
