package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import ApiCalls._
import utils.log

// Model
case class Auth(jwt: Option[String] = None)

// Actions
case class Login(username: String, password: String) extends Action
case class Register(name: String, username: String, email: String, password: String, gender: String) extends Action
case class UserLoggedIn(jwt: String) extends Action
case class UserRegistered(jwt: String) extends Action

// Action handler
class AuthHandler[M](modelRW: ModelRW[M, Option[String]]) extends ActionHandler(modelRW){
  override def handle = {
    case Login(username, password) 
      => effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) 
      => effectOnly(registerEffect(name, username, email, password, gender))
    case UserLoggedIn(token) => updated(Some(token))
    case UserRegistered(token) => updated(Some(token))
  }
}