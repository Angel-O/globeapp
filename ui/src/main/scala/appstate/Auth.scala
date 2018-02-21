package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import ApiCalls._

// Model
case class Auth(jwt: Option[String] = None)

// Actions
case class Login(username: String, password: String) extends Action
case class UserLoggedIn(jwt: String) extends Action

// Action handler
class AuthHandler[M](modelRW: ModelRW[M, Option[String]]) extends ActionHandler(modelRW){
  override def handle = {
    case Login(username, password) => effectOnly(loginEffect(username, password))
    case UserLoggedIn(token) => {
      import utils.log
      log.warn("TOKEN", token)
      updated(Some(token))
    }
  }
}