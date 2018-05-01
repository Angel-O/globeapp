package models.poll

import java.time._
import models.common._

//TODO is it needed??

import play.api.libs.json._
import play.api.libs.functional.syntax._


object PollFormats {
  
  implicit val pollOptionFormat: OFormat[PollOption] = Json.format[PollOption]
  implicit val pollStatusFormat: Format[PollStatus] = PollStatusFormat
  
  implicit val pollReads: Reads[Poll] = (
      (__ \ "title").read[String] and
      (__ \ "content").read[String] and
      (__ \ "mobileAppId").read[String] and
      (__ \ "createdBy").read[String] and
      (__ \ "closingDate").read[LocalDate] and//read[String].map(LocalDate.parse) and
      (__ \ "status").read[PollStatus] and
      (__ \ "options").read[Seq[PollOption]] and
      (__ \ "_id").readNullable[String])(Poll.apply _)
  
  implicit val pollWrites: Writes[Poll] = (
      (__ \ "title").write[String] and
      (__ \ "content").write[String] and
      (__ \ "mobileAppId").write[String] and
      (__ \ "createdBy").write[String] and
      (__ \ "closingDate").write[LocalDate] and
      (__ \ "status").write[PollStatus] and
      (__ \ "options").write[Seq[PollOption]] and
      (__ \ "_id").writeNullable[String])(unlift(Poll.unapply))
      
   //implicit val pollFormat: Format[Poll] = Format(pollReads, pollWrites)
}

object PollStatusFormat extends Format[PollStatus] {
  def reads(json: JsValue): JsResult[PollStatus] = json.as[String].toLowerCase() match{
    case "open" => JsSuccess(Open)
    case "closed" => JsSuccess(Closed)
    case _ => JsError("Invalid Status")
  } 
  def writes(status: PollStatus) = status match {
    case Open => JsString("open")
    case Closed => JsString("closed")
  }
}
