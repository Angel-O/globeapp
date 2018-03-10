package apimodels.user
import upickle.default.{ReadWriter => RW, macroRW}
import play.api.libs.json.{OFormat, Json}

case class User(id: String, username: String)
case object User{
  implicit def rw: RW[User] = macroRW
  implicit val userReads: OFormat[User] = Json.format[User]
  def apply(id: String, username: String) = new User(id, username)
}