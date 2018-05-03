package apimodels.message

import apimodels.common._
import java.time._
import play.api.libs.json.OFormat
import play.api.libs.json.Json

case class WsMessage(
  sender:      String,
  messageType: MessageType,
  dateCreated: Option[LocalDate],
  _id:         Option[String]    = None)
  extends Entity

case object WsMessage {
  implicit val messageFormat: OFormat[WsMessage] = Json.format[WsMessage]

  def apply(
    sender:      String,
    messageType: MessageType,
    dateCreated: Option[LocalDate],
    id:          Option[String]    = None) =
    new WsMessage(sender, messageType, dateCreated, id)
}
