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
import apimodels.user.{User, Role, UserProfile}
import diode.Effect


// Model //TODO rename to AuthState
protected case class AuthState(jwt: Option[String] = None,
                                errorCode: Option[Int] = None,
                                persistentState: Option[PersistentState] = None, //cannot be an option...
                                loggedIn: Option[Boolean] = None,
                                isTokenExpired: Option[Boolean] = None,
                                matchingEmails: Pot[Int] = Pot.empty)
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
                    gender: String,
                    role: String,
                    whereDidYouHearAboutUs: String, 
                    additionalInfo: String, 
                    subscribed: Boolean,
                    favoriteCategories: Seq[String])
    extends Action
case object Logout extends Action
case object VerifyToken extends Action
case class VerifyUserAlreadyRegistered(username: String) extends Action


// Derived Actions
case object UserLoggedOut extends Action
case class UserLoggedIn(jwt: String, user: User) extends Action
case class LoginFailed(errorCode: Int) extends Action
case class CreateUserProfile(
  userId:                 String,
  whereDidYouHearAboutUs: String,
  additionalInfo:         String,
  subscribed:             Boolean,
  favoriteCategories:     Seq[String]) extends Action
case object TokenExpired extends Action
case object TokenValid extends Action
case class StateRestored(state: PersistentState) extends Action
case class MatchingEmailsCount(count: Int) extends Action
case object VerifyUserAlreadyRegisteredFailed extends Action



// Action handler
class AuthHandler[M](modelRW: ModelRW[M, AuthState])
    extends ActionHandler(modelRW)
    with AuthEffects {
  override def handle = {
    case Login(username, password) =>
      effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender, role, whereDidYouHearAboutUs, additionalInfo, subscribed, favoriteCategories) =>
      effectOnly(registerEffect(name, username, email, password, gender, role, whereDidYouHearAboutUs, additionalInfo, subscribed, favoriteCategories))
    case Logout => effectOnly(logoutEffect()) // + redirectEffect(HomePageURI))
    case UserLoggedIn(token, user) => {
      val state = value.persistentState
      .map(state => state.copy(user = user))
      .getOrElse(PersistentState(user))
      updated(AuthState(
        jwt = Some(token),
        persistentState = Some(state),
        loggedIn = Some(true),
        isTokenExpired = Some(false)),
        storeTokenEffect(token) + persistStorageEffect(state) + redirectEffect(ROOT_PATH))
    }
//    case CreateUserProfile(userId, whereDidYouHearAboutUs, additionalInfo, subscribed, favoriteCategories) => { println("HELLOWORLD")
//      effectOnly(createUserProfileEffect(userId, whereDidYouHearAboutUs, additionalInfo, subscribed, favoriteCategories))}
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
    case VerifyUserAlreadyRegistered(username) =>
      // reset first
      val pendingResult = value.matchingEmails.pending()
      updated(
        value.copy(matchingEmails = pendingResult),
        verifyEmailAlreadyTakenEffect(username))
    case MatchingEmailsCount(count) =>
      val readyResult = value.matchingEmails.ready(count)
      updated(value.copy(matchingEmails = readyResult))
    case VerifyUserAlreadyRegisteredFailed => noChange //TODO mark as failed
  }
}


// Effects
trait AuthEffects extends Push{ //Note: AuthEffects cannot be an object extending Push: it causes compliation around imports...
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  //import scala.util.Try
  import apimodels.user.User
  import utils.api._, utils.jwt._, utils.persist._, utils.redirect._
  import diode.{Effect, NoAction}
  import config._
  import org.scalajs.dom.raw.XMLHttpRequest

  def loginEffect(username: String, password: String) = {
    val payload = User(username, password = Some(password)) // no id
    Effect((for {
      xhr <- Post(url = s"$AUTH_SERVER_ROOT/auth/api/login", payload = write(payload))
      token <- readTokenFromResponse(xhr)
      user <- decodeToken(token)
      loginAction <- Future.successful { UserLoggedIn(token, user) }
    } yield (loginAction)).recover({ case ex => LoginFailed(getErrorCode(ex)) }))
  }
  def registerEffect(
    name:                   String,
    username:               String,
    email:                  String,
    password:               String,
    gender:                 String,
    role:                   String,
    whereDidYouHearAboutUs: String,
    additionalInfo:         String,
    subscribed:             Boolean,
    favoriteCategories:     Seq[String]) = {
    val payload = User(
      username,
      role,
      name = Some(name),
      email = Some(email),
      password = Some(password),
      gender = Some(gender))
    Effect((for {
      xhr <- Post(url = s"$AUTH_SERVER_ROOT/auth/api/register", payload = write(payload))
      token <- readTokenFromResponse(xhr)
      user <- decodeToken(token) andThen { // andThen used for side effects...if it fails the error is not caught by redirectOnFailure
          case user => createUserProfile(
            user.get._id, // calling get on the Try ...this should not fail
            whereDidYouHearAboutUs,
            additionalInfo, 
            subscribed, 
            favoriteCategories)
        }
      loginAction <- Future.successful { UserLoggedIn(token, user) }
    } yield (loginAction)).redirectOnFailure) 
  }
  private def createUserProfile(
    maybeId:                Option[String],
    whereDidYouHearAboutUs: String,
    additionalInfo:         String,
    subscribed:             Boolean,
    favoriteCategories:     Seq[String]) = {

    maybeId.map(id => { 
      import apimodels.mobile.Genre.asGenre
      val userProfile = UserProfile(id, whereDidYouHearAboutUs, additionalInfo, subscribed, favoriteCategories.map(asGenre))
      Post(url = s"$USERPROFILE_SERVER_ROOT/api/userprofiles", payload = write(userProfile))
    })
  }
  def logoutEffect() = {
    Effect(Get(url = s"$AUTH_SERVER_ROOT/auth/api/logout")
      .map(_ => UserLoggedOut))
  }
  def verifyEmailAlreadyTakenEffect(email: String) = {
    Effect(Post(url = s"$AUTH_SERVER_ROOT/api/emails", payload = email, contentHeader = TEXT_CONTENT_HEADER)
      .map(xhr => MatchingEmailsCount(xhr.responseText.toInt))
      .recover({ case _ => VerifyUserAlreadyRegisteredFailed }))
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
      .map(_ => TokenValid)
      .recover({ case _ => TokenExpired })) //TODO add status code to provide proper info
  }
  def persistStorageEffect(state: PersistentState) = {
    Effect(Future { persist(state) }.map(_ => NoAction))
  }
  def wipeStorageEffect() = {
    Effect(Future { wipe() }.map(_ => NoAction))
  }
  def restoreFromStorageEffect() = {
    Effect(
      (for {
        storageState <- Future { retrieve() }
        action <- Future { storageState.map(state => StateRestored(state)) }
      } yield (action).getOrElse(NoAction)))
  }
  private def readTokenFromResponse(xhr: XMLHttpRequest) = {
    Future { xhr.getResponseHeader(AUTHORIZATION_HEADER_NAME) }
  }
  private def decodeToken(token: String): Future[User] = {
    Future { read[User](decodeJWT(token)) }
  }
  //  def createUserProfileEffect(userId: String, wduhau: String, additionalInfo: String, subscribed: Boolean, favoriteCategories: Seq[String]) = {
//    import apimodels.mobile.Genre.asGenre
//    favoriteCategories.map(asGenre).foreach(println)
//    val userProfile = UserProfile(userId, wduhau, additionalInfo, subscribed, favoriteCategories.map(asGenre))
//    Effect(Post(url = s"$USERPROFILE_SERVER_ROOT/api/userprofiles", payload = write(userProfile)).map(_ => NoAction))
//  }
}

// Selector
object AuthSelector extends AppModelSelector[AuthState] {

  import utils.persist._
  def getToken() = model.jwt
  def getErrorCode() = model.errorCode
  def getUsername(): String = model.persistentState.map(_.user.username).getOrElse("")
  def getUserId(): String = {
    (for {
      appStateUserId <- model.persistentState.flatMap(state => state.user._id)
      storageUserId <- retrieve().flatMap(state => state.user._id)
    } yield (if (appStateUserId.isEmpty) storageUserId else appStateUserId))
    .getOrElse("")
  }
  def getLoggedIn(): Boolean = model.loggedIn.getOrElse(false)
  def getRole(): Option[Role] = model.persistentState.map(_.user.role)
  
  def getMatchingEmailsCount() = model.matchingEmails match{
    case Ready(count) => Some(count)
    case Pending(_) => Some(-1) //dummy value useful to display spinner or similar to ui while waiting for result. TODO add custom error codes
    case _ => None
  }

  val cursor = AppCircuit.authSelector
  val circuit = AppCircuit
}