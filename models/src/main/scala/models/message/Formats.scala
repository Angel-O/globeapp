package models.message

import java.time._

import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

import play.api.libs.json._ 
import play.api.libs.functional.syntax._ 

import models.common, common.Author, common._
import MessageType._

object WsMessageFormats {
  
  implicit val messageReads: Reads[WsMessage] = (
    (__ \ "sender").read[Author] and
    (__ \ "messageType").read[MessageType] and
    (__ \ "dateCreated").readNullable[String].map(maybeDate => maybeDate.map(LocalDate.parse)) and
    (__ \ "_id").readNullable[String])(WsMessage.apply _)

  
  implicit val messageWrites: Writes[WsMessage] = (
    (__ \ "sender").write[Author] and
    (__ \ "content").write[MessageType] and
    (__ \ "dateCreated").writeNullable[LocalDate] and
    (__ \ "_id").writeNullable[String])(unlift(WsMessage.unapply)) 
}

object SimpleMessageTypeFormats{
  implicit val disconnectReads: Reads[ClientDisconnected] = (
    (__ \ "userId").read[String] and
    (__ \ "reason").read[String])(ClientDisconnected.apply _)
    
  implicit val disconnectWrites: Writes[ClientDisconnected] = (
    (__ \ "userId").write[String] and
    (__ \ "reason").write[String])(unlift(ClientDisconnected.unapply))
  
    // NOT WORKING (Reads)
//  implicit val connectReads: Reads[ClientConnected] = (
//    (__ \ "userId").read[String].map)(ClientConnected.apply _)
//    
  implicit val connectWrites: Writes[ClientConnected] = (
    (__ \ "userId").write[String].contramap)(unlift(ClientConnected.unapply))
    
   
  // SEE https://stackoverflow.com/questions/36095574/json-validate-for-play/36121312#36121312
  val standardReads = Json.reads[ClientConnected]

  val strictConnectedReads = new Reads[ClientConnected] {
    val expectedKeys = Set("userId")

    def reads(jsv: JsValue): JsResult[ClientConnected] = {
      standardReads.reads(jsv).flatMap { person =>
        checkUnwantedKeys(jsv, person)
      }
    }

    private def checkUnwantedKeys(jsv: JsValue, cc: ClientConnected): JsResult[ClientConnected] = {
      val obj = jsv.asInstanceOf[JsObject]
      val keys = obj.keys
      val unwanted = keys.diff(expectedKeys)
      if (unwanted.isEmpty) {
        JsSuccess(cc)
      } else {
        JsError(s"Keys: ${unwanted.mkString(",")} found in the incoming JSON")
      }
    }
  } 
    
  implicit val disconnectFormat: Format[ClientDisconnected] = Format(disconnectReads, disconnectWrites)
  implicit val connectFormat: Format[ClientConnected] = Format(strictConnectedReads, connectWrites)
}

object MessageTypeFormat extends Format[MessageType] {
  import play.api.libs.json.JsValue
  import play.api.libs.json.JsString
  import play.api.libs.json.JsResult
  import play.api.libs.json.JsSuccess
  import play.api.libs.json.JsError
  
  import play.api.libs.json.Reads
  import play.api.libs.json.Json
  
  import SimpleMessageTypeFormats._

  // they all have to ahve a diffrent structure...TODO add a tag to avoind this annoying restriction
  // or findo out if there is a solution already in play json lib
  implicit val userMessageFormat: OFormat[UserMessage] = Json.format[UserMessage]
  //implicit val clientConnectedFormat: OFormat[ClientConnected] = Json.format[ClientConnected]
  //implicit val clientDisconnectedFormat: Format[ClientDisconnected] = disconnectFormat
  implicit val notificationFormat: OFormat[Notification] = Json.format[Notification]
  
  def reads(json: JsValue): JsResult[MessageType] = {
    // this would fail wiithout using strict Reads in at least one of the below...
    //println("SEE CONN", json.asOpt[ClientConnected])
    //println("SEE DISCONN", json.asOpt[ClientDisconnected])
    (
      json.asOpt[UserMessage] ::
      json.asOpt[ClientConnected] ::
      json.asOpt[ClientDisconnected] ::
      json.asOpt[Notification] ::
      json.asOpt[String].collect { 
        case "userAuthenticated" => UserAuthenticated  //SAMPLE MESSAGE NOT USED
      } :: Nil find (_.nonEmpty)
    )
    .flatten
    .map { messageType => JsSuccess(messageType) }
    .getOrElse { JsError(s"Invalid message type: ${json.toString}") }
  }
  
  def writes(messageType: MessageType) = messageType match {
    case UserAuthenticated => JsString("userAuthenticated") //SAMPLE MESSAGE NOT USED
    case cc: ClientConnected => connectFormat.writes(cc)
    case cd: ClientDisconnected => disconnectFormat.writes(cd)
    case nt: Notification => notificationFormat.writes(nt)
    case msg: UserMessage => userMessageFormat.writes(msg)
//    case ClientConnected(userId) => JsObject(Seq("userId" -> JsString(userId)))
//    case ClientDisconnected(userId, reason) => JsObject(Seq(
//        "userId" -> JsString(userId), 
//        "reason" -> JsString(reason)))
//    case Notification(recipientId: String, senderId: String) => JsObject(Seq(
//      "recipientId" -> JsString(recipientId),
//      "senderId" -> JsString(senderId))) 
//    case UserMessage(sender, content, recipient, id) => JsObject(Seq(
//      "content" -> JsString(content),
//      "recipient" -> JsString(recipient)))
  }
}