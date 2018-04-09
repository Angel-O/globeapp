package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.Push
import config._
import navigation.URIs._

import diode.data.Pot
import diode.data.PotState._
import diode.data.{Ready, Pending}
import apimodels.user.User
import diode.Effect


// Model //TODO rename to AuthState
protected case class AuthState(jwt: Option[String] = None,
                                errorCode: Option[Int] = None,
                                persistentState: Option[PersistentState] = None, //cannot be an option...
                                loggedIn: Option[Boolean] = None,
                                isTokenExpired: Option[Boolean] = None,
                                matchingUsernames: Pot[Int] = Pot.empty)
case class Auth(params: AuthState)
case object Auth {
  def apply() = new Auth(AuthState())
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
class AuthHandler[M](modelRW: ModelRW[M, AuthState])
    extends ActionHandler(modelRW)
    with AuthEffects {
  override def handle = {
    case Login(username, password) =>
      effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) =>
      effectOnly(registerEffect(name, username, email, password, gender))
    case Logout => effectOnly(logoutEffect()) // + redirectEffect(HomePageURI))
    case UserLoggedIn(token, username, id) => {
      val user = User(username = username, _id = Some(id))
      val state = value.persistentState.map(state => state.copy(user = user))
      .getOrElse(PersistentState(User(username = username, _id = Some(id))))
      updated(AuthState(
        jwt = Some(token),
        persistentState = Some(state),
        loggedIn = Some(true),
        isTokenExpired = Some(false)),
        storeTokenEffect(token) + persistStorageEffect(state) + redirectEffect(ROOT_PATH))
    }
    case UserLoggedOut     => updated(AuthState(), removeTokenEffect() + wipeStorageEffect())
    case LoginFailed(code) => updated(value.copy(errorCode = Some(code)))
    case VerifyToken       => effectOnly(verifyTokenEffect())
    case TokenExpired =>
      updated(value.copy(isTokenExpired = Some(true)),
              redirectEffect(LoginPageURI) + wipeStorageEffect())
    case TokenValid =>
      updated(value.copy(loggedIn = Some(true)), restoreFromStorageEffect())
    case StateRestored(state) =>
      updated(value.copy(persistentState = Some(state)))
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
      .map(xhr => UserLoggedIn(xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME), username, xhr.responseText)))
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
    .map(xhr => { TokenValid })
    .recover({ case _ => TokenExpired })) //TODO add status code to provide proper info
  }
  def persistStorageEffect(state: PersistentState) = {
    Effect(Future{ persist(state) }.map(_ => NoAction) )
  }
  def wipeStorageEffect() = {
    Effect(Future{ wipe() }.map(_ => NoAction) )
  }
  def restoreFromStorageEffect() = {
    Effect(
      (for {
        storageState <- Future { retrieve() }
        action <- Future { storageState.map(state => StateRestored(state)) }
      } yield (action).getOrElse(NoAction)))
  }
}

// Selector
//@safe this trait is necessary....to mix-in with objects..
// trait AuthSelector extends GenericConnect[AppModel, AuthState] {

//  import utils.persist._
//  def getToken() = model.jwt.getOrElse("UNSET")
//  def getErrorCode() = model.errorCode
//  def getUsername() = {
//    //TODO this does not work
//     // (for {
//     //   usernameFromAppState <- model.persistentState.map(_.user.username)
//     //   usernameFromStorage <- retrieve().map(_.user.username)
//     // } yield (
//     //   if(usernameFromAppState.isEmpty) usernameFromStorage else usernameFromAppState 
//     // )).getOrElse("")

//     model.persistentState.map(_.user.username)
//     .getOrElse(retrieve().map(_.user.username).getOrElse(""))
//   }
//   def getUserId() = {
//     // (for {
//     //   appState <- model.persistentState
//     //   storageState <- retrieve()
//     // } yield (Some(appState.user._id)
//     //   .getOrElse(storageState.user._id)))
//     //   .flatten

//      model.persistentState.map(_.user._id.getOrElse(retrieve().map(_.user._id).getOrElse("")))
//   }
//  def getLoggedIn() = model.loggedIn.getOrElse(false)
//  def getMatchingUsernamesCount() = model.matchingUsernames.state match{
//    case PotReady => Some(model.matchingUsernames.get)
//    case PotPending => Some(-1) //dummy value useful to display spinner or similar to ui while waiting for result
//    case _ => None
//  }

//  val cursor = AppCircuit.authSelector
//  val circuit = AppCircuit
//  connect()
// }

object AuthSelector extends ReadConnect[AppModel, AuthState] {

  import utils.persist._
  def getToken() = model.jwt.getOrElse("UNSET")
  def getErrorCode() = model.errorCode
  def getUsername(): String = model.persistentState.map(_.user.username).getOrElse("")
  def getUserId(): String = {
    (for {
      appStateUserId <- model.persistentState.flatMap(state => state.user._id)
      storageUserId <- retrieve().flatMap(state => state.user._id)
    } yield (if (appStateUserId.isEmpty) storageUserId else appStateUserId))
    .getOrElse("")
  }
  def getLoggedIn() = model.loggedIn.getOrElse(false)
  
  def getMatchingUsernamesCount() = model.matchingUsernames.state match{
    case PotReady => Some(model.matchingUsernames.get)
    case PotPending => Some(-1) //dummy value useful to display spinner or similar to ui while waiting for result. TODO add custom error codes
    case _ => None
  }

  val cursor = AppCircuit.authSelector
  val circuit = AppCircuit
}
