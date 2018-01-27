package apimodels
import upickle.default.{ReadWriter => RW, macroRW}

case object User{
  implicit def rw: RW[User] = macroRW
}

case class User(name: String, id: Int)