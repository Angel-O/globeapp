//package models
//import reactivemongo.bson.BSONObjectID
//import scala.language.postfixOps
//import apimodels.poll.{ Poll => ApiPoll, PollOption }
//import java.time.LocalDate
//
//case class Poll private (
//  _id:         BSONObjectID,
//  title:       String,
//  content:     String,
//  mobileAppId: BSONObjectID,
//  createdBy:   String,
//  closingDate: LocalDate,
//  status:      String,
//  options:     Seq[PollOption])
//
//case object Poll {
//  import play.api.libs.json._
//  import play.api.libs.functional.syntax._
//  import reactivemongo.play.json._
//
//  def apply(
//    title:       String,
//    content:     String,
//    mobileAppId: String,
//    createdBy:   String,
//    closingDate: LocalDate,
//    status:      String,
//    options:     Seq[PollOption]) = {
//
//    val _id: BSONObjectID = BSONObjectID.generate
//    val _mobileAppId: BSONObjectID = BSONObjectID.parse(mobileAppId).get
//    new Poll(
//      _id,
//      title,
//      content,
//      _mobileAppId,
//      createdBy,
//      closingDate,
//      status,
//      options)
//  }
//
//  def apply(apiPoll: ApiPoll) = {
//    new Poll(
//      BSONObjectID.parse(apiPoll.id).get,
//      apiPoll.title,
//      apiPoll.content,
//      BSONObjectID.parse(apiPoll.mobileAppId).get,
//      apiPoll.createdBy,
//      apiPoll.closingDate,
//      apiPoll.status,
//      apiPoll.options)
//  }
//
//  implicit val pollOptionformat: OFormat[PollOption] = Json.format[PollOption]
//  implicit val pollFormat: OFormat[Poll] = Json.format[Poll]
//}
//
////object ConversionHelpers {
////  implicit class fromModelToApi(x: Poll) {
////    def toApi =
////      ApiPoll(
////        x._id.stringify,
////        x.title,
////        x.content,
////        x.mobileAppId.stringify,
////        x.createdBy,
////        x.closingDate,
////        x.status,
////        x.options)
////  }
////  implicit class fromApiToModel(x: ApiPoll) {
////    def toModel =
////      Poll(
////        BSONObjectID.parse(x.id).get,
////        x.title,
////        x.content,
////        BSONObjectID.parse(x.mobileAppId).get,
////        x.createdBy,
////        x.closingDate,
////        x.status,
////        x.options)
////  }
////}
