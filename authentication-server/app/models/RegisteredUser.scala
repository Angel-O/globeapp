package models
import reactivemongo.bson.BSONObjectID
import scala.language.postfixOps

case class RegisteredUser private(
    _id: BSONObjectID, 
    name: String, 
    username: String, 
    email: String, 
    password: String, 
    gender: String)
    
    
case object RegisteredUser{
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import reactivemongo.play.json._
  
  def apply (
    name: String, 
    username: String, 
    email: String, 
    password: String, 
    gender: String) = {
    
    val _id: BSONObjectID = BSONObjectID.generate
    new RegisteredUser(_id, name, username, email, password, gender)
  }
  
  implicit val userFormat: OFormat[RegisteredUser] = Json.format[RegisteredUser]
//  implicit val userReads: Reads[RegisteredUser] = (
//    (JsPath \ "_id").read[BSONObjectID] and
//    (JsPath \ "name").read[String] and
//    (JsPath \ "username").read[String] and
//    (JsPath \ "email").read[String] and
//    (JsPath \ "password").read[String] and
//    (JsPath \ "gender").read[String]
//    )(RegisteredUser.apply _)
//    
//   implicit val usertWrites: OWrites[RegisteredUser] = (
//    (JsPath \ "_id").write[BSONObjectID] and//.map(x => Some(x.get.stringify)) and
//    (JsPath \ "name").write[String] and
//    (JsPath \ "username").write[String] and
//    (JsPath \ "email").write[String] and
//    (JsPath \ "password").write[String] and
//    (JsPath \ "gender").write[String]
//    )(unlift(RegisteredUser.unapply))
}