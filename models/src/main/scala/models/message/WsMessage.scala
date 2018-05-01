package models.message

import models.common, common.Author, common.Author._, common._
import java.time._
import play.api.libs.json.OFormat
import play.api.libs.json.Json

//TODO the author could just be a user...
case class WsMessage(
  sender:      Author,
  messageType: MessageType,
  dateCreated: Option[LocalDate],
  _id:         Option[String]    = None)
  extends Entity

case object WsMessage {
  implicit val messageFormat: OFormat[WsMessage] = Json.format[WsMessage]

  def apply(
    sender:      Author,
    messageType: MessageType,
    dateCreated: Option[LocalDate],
    id:          Option[String]    = None) =
    new WsMessage(sender, messageType, dateCreated, id)
}
