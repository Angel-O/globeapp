package apimodels
import upickle.default.{ReadWriter => RW, macroRW}

sealed trait ApiModel

case object User extends ApiModel{
  implicit def rw: RW[User] = macroRW
  def apply(name: String, _id: Option[String] = None) = new User(name, _id)
}

case class User(name: String, _id: Option[String] = None)

case object LoginDetails{
  implicit def rw: RW[LoginDetails] = macroRW
  def apply(username: String, password: String) = new LoginDetails(username, password)
}
case class LoginDetails(username: String, password: String)

//case object RegistrationDetails{
//  implicit def rw: RW[RegistrationDetails] = macroRW
//  def apply(username: String, password: String) = new RegistrationDetails(username, password)
//}
//case class RegistrationDetails(name: String, username: String, email: String, password: String, gender: Gender)