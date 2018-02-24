package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.{Push, safe}
import config._
import navigation.URIs._

// Model //TODO rename to AuthState
protected case class AuthParams(jwt: Option[String] = None, errorCode: Option[Int] = None)
case class Auth(params: AuthParams)
case object Auth {
  def apply() = new Auth(AuthParams())
}

// Primary Actions
case class Login(username: String, password: String)
    extends Action
case class Register(name: String,
                    username: String,
                    email: String,
                    password: String,
                    gender: String)
    extends Action
case object Logout extends Action


// Derived Actions
case object UserLoggedOut extends Action
case class UserLoggedIn(jwt: String) extends Action
case class LoginFailed(errorCode: Int) extends Action
case class UserRegistered(jwt: String) extends Action



// Action handler
class AuthHandler[M](modelRW: ModelRW[M, AuthParams])
    extends ActionHandler(modelRW)
    with AuthEffects {
  override def handle = {
    case Login(username, password) =>
      effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) =>
      effectOnly(registerEffect(name, username, email, password, gender))
    case Logout => effectOnly(logoutEffect() + redirectEffect(LoginPageURI))
    case UserLoggedIn(token) =>
      updated(AuthParams(jwt = Some(token)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH))
    case UserRegistered(token) =>
      updated(AuthParams(jwt = Some(token)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH))
    case UserLoggedOut     => updated(AuthParams(), removeTokenEffect())
    case LoginFailed(code) => updated(AuthParams(errorCode = Some(code)))
  }
}


// Effects
trait AuthEffects extends Push{ //Note: AuthEffects cannot be an object extending Push: it causes compliation around imports...
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import upickle.default._
  import apimodels.{LoginDetails, RegistrationDetails}
  import utils.api._, utils.jwt._
  import diode.{Effect, NoAction}
  
  def loginEffect(username: String, password: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/auth/api/login", payload = write(LoginDetails(username, password)))
        .map(xhr => UserLoggedIn(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME)))
        .recover({ case ex => LoginFailed(getStatusCode(ex)) }))
  }
  def registerEffect(name: String, username: String, email: String, password: String, gender: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/auth/api/register", payload = write(RegistrationDetails(name, username, email, password, gender)))
        .map(xhr => UserRegistered(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME))))
  }
  def logoutEffect() = {
    Effect(Get(url = s"$AUTH_SERVER_ROOT/auth/api/logout")
        .map(_ => UserLoggedOut))
  }
  def redirectEffect(path: String) = {
    Effect(Future{ push(path) }.map(_ => NoAction))
  }
  def storeTokenEffect(token: String) = {
    Effect(Future{ storeToken(token) }.map(_ => NoAction))
  }
  def removeTokenEffect() = {
    Effect(Future{ removeToken }.map(_ => NoAction))
  }
}

// Selector
@safe
trait AuthSelector extends {
  override val cursor = AppCircuit.authSelector
  override val circuit = AppCircuit
} with GenericConnect[AppModel, AuthParams] {

  def getToken() = value.jwt.getOrElse("OOO")
  def getErrorCode() = value.errorCode.getOrElse(0)

  connect()
}


// trait AuthSelector extends Connect{
//   //val getToken = () => AppCircuit.currentModel.auth.params.jwt.getOrElse("OOO")
//   val getToken = () => AppCircuit.authSelector.value.jwt.getOrElse("OOO")
//   val getErrorCode = () => value.auth.params.errorCode.getOrElse(0)

//   def connectWith(): Unit
//   connect()(AppCircuit.authSelector, connectWith())
// }
