package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.{Push} //safe}
import config._
import navigation.URIs._

//import upickle.default.{ReadWriter => RW, macroRW}

// Model //TODO rename to AuthState
protected case class AuthParams(jwt: Option[String] = None,
                                errorCode: Option[Int] = None,
                                username: Option[String] = None,
                                loggedIn: Option[Boolean] = None,
                                isTokenExpired: Option[Boolean] = None)
case class Auth(params: AuthParams)
case object Auth {
  def apply() = new Auth(AuthParams())
  // implicit def rw: RW[Auth] = macroRW
  // def apply(username: Option[String]) = Auth.apply()
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
case object VerifyToken extends Action


// Derived Actions
case object UserLoggedOut extends Action
case class UserLoggedIn(jwt: String, username: String) extends Action
case class LoginFailed(errorCode: Int) extends Action
case class UserRegistered(jwt: String, username: String) extends Action
case object TokenExpired extends Action
case object TokenValid extends Action
case class StateRestored(state: PersistentState) extends Action



// Action handler 
class AuthHandler[M](modelRW: ModelRW[M, AuthParams])
    extends ActionHandler(modelRW)
    with AuthEffects {
  override def handle = {
    case Login(username, password) =>
      effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) =>
      effectOnly(registerEffect(name, username, email, password, gender))
    case Logout => effectOnly(logoutEffect())// + redirectEffect(HomePageURI))
    case UserLoggedIn(token, username) =>
      updated(AuthParams(jwt = Some(token),
                         username = Some(username),
                         loggedIn = Some(true),
                         isTokenExpired = Some(false)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH) + persistStorageEffect(username))
    case UserRegistered(token, username) =>
      updated(value.copy(jwt = Some(token),
                         username = Some(username),
                         loggedIn = Some(true),
                         isTokenExpired = Some(false)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH) + persistStorageEffect(username))
    case UserLoggedOut     => updated(AuthParams(), removeTokenEffect() + wipeStorageEffect())
    case LoginFailed(code) => updated(value.copy(errorCode = Some(code)))
    case VerifyToken       => effectOnly(verifyTokenEffect())
    case TokenExpired =>
      updated(value.copy(isTokenExpired = Some(true)),
              redirectEffect(LoginPageURI) + wipeStorageEffect())
    case TokenValid => updated(value.copy(loggedIn = Some(true)), restoreFromStorageEffect())
    case StateRestored(state) => updated(value.copy(username = Some(state.username))) 
  }
}


// Effects
trait AuthEffects extends Push{ //Note: AuthEffects cannot be an object extending Push: it causes compliation around imports...
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import upickle.default._
  import apimodels.{LoginDetails, RegistrationDetails}
  import utils.api._, utils.jwt._, utils.persist._
  import diode.{Effect, NoAction}
  
  def loginEffect(username: String, password: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/auth/api/login", payload = write(LoginDetails(username, password)))
        .map(xhr => UserLoggedIn(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME), username))
        .recover({ case ex => LoginFailed(getErrorCode(ex)) }))
  }
  def registerEffect(name: String, username: String, email: String, password: String, gender: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/auth/api/register", payload = write(RegistrationDetails(name, username, email, password, gender)))
        .map(xhr => UserRegistered(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME), username)))
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
  def verifyTokenEffect() = {
    Effect(Get(url = s"$AUTH_SERVER_ROOT/api/verify")
    .map(xhr => {TokenValid})
    .recover{case _ => TokenExpired}) //TODO add status code to provide proper info
  }
  def persistStorageEffect(username: String) = {
    Effect(Future{ persist(PersistentState(username)) }.map(_ => NoAction) )
  }
  def wipeStorageEffect() = {
    Effect(Future{ wipe() }.map(_ => NoAction) )
  }
  def restoreFromStorageEffect() = {
    Effect(Future{ retrieve() }.map(state => StateRestored(state)) )
  }
}

// Selector
//@safe
trait AuthSelector extends GenericConnect[AppModel, AuthParams] {

  import utils.persist._
  def getToken() = model.jwt.getOrElse("OOO")
  def getErrorCode() = model.errorCode
  def getUsername() = model.username.getOrElse(retrieve().username)
  def getLoggedIn() = model.loggedIn.getOrElse(false)
  
  val cursor = AppCircuit.authSelector
  val circuit = AppCircuit
  connect()
}

// trait AuthSelector extends Connect{
//   //val getToken = () => AppCircuit.currentModel.auth.params.jwt.getOrElse("OOO")
//   val getToken = () => AppCircuit.authSelector.value.jwt.getOrElse("OOO")
//   val getErrorCode = () => value.auth.params.errorCode.getOrElse(0)

//   def connectWith(): Unit
//   connect()(AppCircuit.authSelector, connectWith())
// }
