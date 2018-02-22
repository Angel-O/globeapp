package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import ApiCalls._
import utils.log

// Model
case class AuthParams(jwt: Option[String] = None, errorCode: Option[Int] = None)
case class Auth(params: AuthParams)
case object Auth{
  def apply() = new Auth(AuthParams())
}

// Actions
case class Login(username: String, password: String) extends Action
case class Register(name: String, username: String, email: String, password: String, gender: String) extends Action
case class UserLoggedIn(jwt: String) extends Action
case class LoginFailed(errorCode: Int) extends Action //TODO use pot actions...
// case class LoginFailed(potResult: Pot[Seq[User]] = Empty) extends PotAction[Seq[User], UsersFetched]{
//   def next(newResult: Pot[Seq[User]]) = UsersFetched(newResult)
// }
case class UserRegistered(jwt: String) extends Action

// Action handler
class AuthHandler[M](modelRW: ModelRW[M, AuthParams]) extends ActionHandler(modelRW){
  override def handle = {
    case Login(username, password) 
      => effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) 
      => effectOnly(registerEffect(name, username, email, password, gender))
    case UserLoggedIn(token) => updated(AuthParams(jwt = Some(token)))
    case UserRegistered(token) => updated(AuthParams(jwt = Some(token)))
    case LoginFailed(code) => updated(AuthParams(errorCode = Some(code)))
  }
}

// Selector
object AuthSelector {
  val getToken = () => AppCircuit.currentModel.auth.params.jwt.getOrElse("")
}