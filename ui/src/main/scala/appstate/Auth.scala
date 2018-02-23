package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import AuthEffects._
import utils.{log, Push}
import org.scalajs.dom.window
import diode.Effect

// Model
case class AuthParams(jwt: Option[String] = None, errorCode: Option[Int] = None)
case class Auth(params: AuthParams)
case object Auth {
  def apply() = new Auth(AuthParams())
}

// Actions : Note these actions take a callback that will be passed
// to the action invoked in the corresponding effect. Afer a user logged in
// or registered the call back will trigger the navigation to the home page
case class Login(username: String, password: String)
    extends Action
case class Register(name: String,
                    username: String,
                    email: String,
                    password: String,
                    gender: String)
    extends Action
case object Logout extends Action
case object UserLoggedOut extends Action
case class UserLoggedIn(jwt: String) extends Action
case class LoginFailed(errorCode: Int) extends Action //TODO use pot actions...
// case class LoginFailed(potResult: Pot[Seq[User]] = Empty) extends PotAction[Seq[User], UsersFetched]{
//   def next(newResult: Pot[Seq[User]]) = UsersFetched(newResult)
// }
case class UserRegistered(jwt: String) extends Action

// Action handler
class AuthHandler[M](modelRW: ModelRW[M, AuthParams])
    extends ActionHandler(modelRW) with Push {
  override def handle = {
    case Login(username, password) =>
      effectOnly(loginEffect(username, password))
    case Register(name, username, email, password, gender) =>
      effectOnly(registerEffect(name, username, email, password, gender))
    case Logout => effectOnly(logoutEffect())
    case UserLoggedIn(token) => {
      storeToken(token) //TODO turn this into an effect..
      navigateToHome()
      updated(AuthParams(jwt = Some(token)))
    }
    case UserRegistered(token) => {
      storeToken(token)
      navigateToHome()
      updated(AuthParams(jwt = Some(token)))
    }
    case UserLoggedOut => {
      removeToken()
      updated(AuthParams())
    }
    case LoginFailed(code) => updated(AuthParams(errorCode = Some(code)))
  }

  private def storeToken(token: String) =
    window.sessionStorage.setItem("Token", token)

  //TODO move this and above to jwt middleware along with getToken used in api middleware
  private def removeToken() =
    window.sessionStorage.removeItem("Token") 

  private def navigateToHome() = push("/") //TODO move this to navigation package
}

// Effects
object AuthEffects{
  import scala.concurrent.ExecutionContext.Implicits.global
  import upickle.default._
  import apimodels.LoginDetails
  import apimodels.RegistrationDetails
  import utils.api._ , utils.log

  def loginEffect(username: String, password: String) = {
    log.warn("payload", write(LoginDetails(username, password)))
    
    Effect(Post(url = "http://localhost:3000/auth/api/login", payload = write(LoginDetails(username, password)))
        .map(xhr => UserLoggedIn(xhr.getResponseHeader("Token"))) //TODO unexpose authorization header from server
        .recover({ case ex => LoginFailed(getStatusCode(ex)) }))
  }
  def registerEffect(name: String, username: String, email: String, password: String, gender: String) = {
    log.warn("payload", write(RegistrationDetails(name, username, email, password, gender)))
    
    Effect(Post(url = "http://localhost:3000/auth/api/register", payload = write(RegistrationDetails(name, username, email, password, gender)))
        .map(xhr => UserRegistered(xhr.getResponseHeader("Token"))))
  }
  def logoutEffect() = {
    Effect(Get(url = "http://localhost:3000/auth/api/logout")
        .map(_ => UserLoggedOut))
  }
  // def fetchUserNamesEffect() = {
  //   Effect(Get(url = "http://localhost:3000/auth/api/usernames")
  //       .map(xhr => UsersFetched(Ready(read[Seq[User]](xhr.responseText))))
  //       .recover({ case ex => UsersFetched(Failed(ex)) }))
  // }
}


// Selector
object AuthSelector {
  val getToken = () => AppCircuit.currentModel.auth.params.jwt.getOrElse("")
}
