package models.user

import play.api.libs.json.{ OFormat, Json }
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.Format
import play.api.libs.json.Reads
import play.api.libs.json.Writes

import Role._

object GenderFormat extends Format[Gender] {
  def reads(json: JsValue): JsResult[Gender] = json.as[String].toLowerCase() match{
    case "male" => JsSuccess(Male)
    case "female" => JsSuccess(Female)
    case _ => JsError("Invalid Gender")
  } 
  def writes(gender: Gender) = gender match {
    case Male => JsString("male")
    case Female => JsString("female")
  }
  
//  implicit val genderReads = Reads[Gender](js =>
//    js.validate[String].map[Gender](identity)
//  )
//  implicit val genderWrites: Writes[Gender] = new Writes[Gender] {
//    def writes(gender: Gender): JsValue = JsString(gender)
//  }
//  implicit val genderFormat: Format[Gender] = Format(genderReads, genderWrites)
}

object RoleFormat extends Format[Role] {
  def reads(json: JsValue): JsResult[Role] = json.as[String].toLowerCase() match{
    case "appuser" => JsSuccess(AppUser)
    case "appdev" => JsSuccess(AppDev)
    case "admin" => JsSuccess(Admin)
    case _ => JsError("Invalid Role")
  } 
  def writes(role: Role) = role match {
    case AppUser => JsString("appuser")
    case AppDev => JsString("appdev")
    case Admin => JsString("admin")
  }
}