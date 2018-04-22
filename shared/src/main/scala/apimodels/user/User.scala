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

sealed trait Role
case object AppUser extends Role
case object AppDev extends Role
case object Admin extends Role
case object Role {
  import RoleFormat._
  import scala.language.implicitConversions
  implicit def asString(role: Role): String = writes(role).as[String]
  implicit def asGender(role: String): Role = reads(JsString(role))
    .asOpt.getOrElse(throw new IllegalArgumentException("Invalid role"))
}

case class User(
  username: String,
  role:     Role           = AppUser,
  _id:      Option[String] = None,
  name:     Option[String] = None,
  password: Option[String] = None,
  email:    Option[String] = None,
  gender:   Option[Gender] = None) extends Entity

case object User {
  // looks like the order of this format is important
  // swapping role format and gender format has the effect
  // of a compiliation failure
  implicit val roleFormat = RoleFormat
  implicit val genderFormat = GenderFormat
  implicit val userFormat: OFormat[User] = Json.format[User]
  def apply(
    username: String,
    role:     Role           = AppUser,
    _id:      Option[String] = None,
    name:     Option[String] = None,
    password: Option[String] = None,
    email:    Option[String] = None,
    gender:   Option[Gender] = None) = new User(username, role, _id, name, password, email, gender)
}