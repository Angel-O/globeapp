package apimodels
import upickle.default.{ReadWriter => RW, macroRW}

sealed trait ApiModel

case object User extends ApiModel{
  implicit def rw: RW[User] = macroRW
  def apply(id: String, username: String) = new User(id, username)
}

case class User(id: String, username: String)

case object LoginDetails{
  implicit def rw: RW[LoginDetails] = macroRW
  def apply(username: String, password: String) = new LoginDetails(username, password)
}
case class LoginDetails(username: String, password: String)

case object RegistrationDetails{
  implicit def rw: RW[RegistrationDetails] = macroRW
  def apply(name: String, username: String, email: String, password: String, gender: String) = 
    new RegistrationDetails(name: String, username: String, email: String, password: String, gender: String)
}
case class RegistrationDetails(name: String, username: String, email: String, password: String, gender: String)