package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.{Push} //safe}
import config._
import navigation.URIs._

import diode.data.Pot
import diode.data.PotState._
import diode.data.{Ready, Pending}

// Model //TODO rename to AuthState
protected case class AuthParams(jwt: Option[String] = None,
                                errorCode: Option[Int] = None,
                                username: Option[String] = None,
                                loggedIn: Option[Boolean] = None,
                                isTokenExpired: Option[Boolean] = None,
                                matchingUsernames: Pot[Int] = Pot.empty,
                                id: Option[String] = None)
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
case object VerifyToken extends Action
case class VerifyUsernameAlreadyTaken(username: String) extends Action


// Derived Actions
case object UserLoggedOut extends Action
case class UserLoggedIn(jwt: String, username: String, id: String) extends Action
case class LoginFailed(errorCode: Int) extends Action
case class UserRegistered(jwt: String, username: String, id: String) extends Action
case object TokenExpired extends Action
case object TokenValid extends Action
case class StateRestored(state: PersistentState) extends Action
case class MatchingUsernamesCount(count: Int) extends Action
case object VerifyUsernameAlreadyTakenFailed extends Action



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
    case UserLoggedIn(token, username, id) =>
      updated(AuthParams(jwt = Some(token),
                         username = Some(username),
                         loggedIn = Some(true),
                         isTokenExpired = Some(false),
                         id = Some(id)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH) + persistStorageEffect(username))
    case UserRegistered(token, username, id) =>
      updated(value.copy(jwt = Some(token),
                         username = Some(username),
                         loggedIn = Some(true),
                         isTokenExpired = Some(false),
                         id = Some(id)),
              storeTokenEffect(token) + redirectEffect(ROOT_PATH) + persistStorageEffect(username))
    case UserLoggedOut     => updated(AuthParams(), removeTokenEffect() + wipeStorageEffect())
    case LoginFailed(code) => updated(value.copy(errorCode = Some(code)))
    case VerifyToken       => effectOnly(verifyTokenEffect())
    case TokenExpired =>
      updated(value.copy(isTokenExpired = Some(true)),
              redirectEffect(LoginPageURI) + wipeStorageEffect())
    case TokenValid =>
      updated(value.copy(loggedIn = Some(true)), restoreFromStorageEffect())
    case StateRestored(state) =>
      updated(value.copy(username = Some(state.username)))
    case VerifyUsernameAlreadyTaken(username) =>
      // reset first
      val pendingResult = value.matchingUsernames.pending()
      updated(value.copy(matchingUsernames = pendingResult),
              verifyUsernameAlreadyTakenEffect(username))
    case MatchingUsernamesCount(count) =>
      val readyResult = value.matchingUsernames.ready(count)
      updated(value.copy(matchingUsernames = readyResult))
    case VerifyUsernameAlreadyTakenFailed => noChange //TODO mark as failed
  }
}


// Effects
trait AuthEffects extends Push{ //Note: AuthEffects cannot be an object extending Push: it causes compliation around imports...
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import apimodels.user.User
  import utils.api._, utils.jwt._, utils.persist._
  import diode.{Effect, NoAction}
  import config._

  
  def loginEffect(username: String, password: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/auth/api/login", payload = write(User(username = username, password = Some(password))))
        .map(xhr => UserLoggedIn(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME), username, xhr.responseText))
        .recover({ case ex => LoginFailed(getErrorCode(ex)) }))
  }
  def registerEffect(name: String, username: String, email: String, password: String, gender: String) = {
    Effect(Post(
        url = s"$AUTH_SERVER_ROOT/auth/api/register", 
        payload = write(User(name = Some(name), username = username, email = Some(email), password = Some(password), gender = Some(gender))))
      .map(xhr => UserRegistered(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME), username, xhr.responseText)))
  }
  def logoutEffect() = {
    Effect(Get(url = s"$AUTH_SERVER_ROOT/auth/api/logout")
        .map(_ => UserLoggedOut))
  }
  def verifyUsernameAlreadyTakenEffect(username: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/api/usernames", payload = username, contentHeader = TEXT_CONTENT_HEADER)
        .map(xhr => MatchingUsernamesCount(xhr.responseText.toInt))
        .recover({ case _ => VerifyUsernameAlreadyTakenFailed }))
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
    .recover({ case _ => TokenExpired })) //TODO add status code to provide proper info
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
  def getToken() = model.jwt.getOrElse("UNSET")
  def getErrorCode() = model.errorCode
  def getUsername() = model.username.getOrElse(retrieve().username)
  def getLoggedIn() = model.loggedIn.getOrElse(false)
  def getMatchingUsernamesCount() = model.matchingUsernames.state match{
    case PotReady => Some(model.matchingUsernames.get)
    case PotPending => Some(-1) //dummy value useful to display spinner or similar to ui while waiting for result
    case _ => None
  }

  val cursor = AppCircuit.authSelector
  val circuit = AppCircuit
  connect()
}

object AuthSelector extends ReadConnect[AppModel, AuthParams] {

  import utils.persist._
  def getToken() = model.jwt.getOrElse("UNSET")
  def getErrorCode() = model.errorCode
  def getUsername() = model.username.getOrElse(retrieve().username)
  def getUserId() = model.id.getOrElse("")
  def getLoggedIn() = model.loggedIn.getOrElse(false)
  def getMatchingUsernamesCount() = model.matchingUsernames.state match{
    case PotReady => Some(model.matchingUsernames.get)
    case PotPending => Some(-1) //dummy value useful to display spinner or similar to ui while waiting for result
    case _ => None
  }

  val cursor = AppCircuit.authSelector
  val circuit = AppCircuit
}
