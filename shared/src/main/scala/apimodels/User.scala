package apimodels
import upickle.default.{ReadWriter => RW, macroRW}

sealed trait ApiModel

case object User extends ApiModel{
  implicit def rw: RW[User] = macroRW
}

case class User(name: String, id: Int)