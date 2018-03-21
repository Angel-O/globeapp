package apimodels.user

import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import apimodels.common.Entity

sealed trait Gender
case object Male extends Gender
case object Female extends Gender
case object Gender {
  import GenderFormat._
  import scala.language.implicitConversions
  implicit def asString(gender: Gender): String = writes(gender).as[String]
  implicit def asGender(gender: String): Gender = reads(JsString(gender))
    .asOpt.getOrElse(throw new IllegalArgumentException("Invalid gender"))
}

case class User(
  _id:      Option[String] = None,
  name:     Option[String] = None,
  username: String,
  password: Option[String] = None,
  email:    Option[String] = None,
  gender:   Option[Gender] = None) extends Entity

case object User {
  implicit val genderFormat = GenderFormat
  implicit val userFormat: OFormat[User] = Json.format[User]
  def apply(
    _id:      Option[String] = None,
    name:     Option[String] = None,
    username: String,
    password: Option[String] = None,
    email:    Option[String] = None,
    gender:   Option[Gender] = None) = new User(_id, name, username, password, email, gender)
}