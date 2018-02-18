package apimodels
import upickle.default.{ReadWriter => RW, macroRW}

sealed trait ApiModel

case object User extends ApiModel{
  implicit def rw: RW[User] = macroRW
  def apply(name: String, _id: Option[String] = None) = new User(name, _id)
}

case class User(name: String, _id: Option[String] = None)