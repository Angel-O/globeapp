package controllers
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.user.User, User._
import javax.inject.Inject
import pdi.jwt.JwtSession._
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.Request
import repos.UserRepository
import utils.Bson._
import utils.FutureImplicits._
import utils.Json._

class AuthenticationController @Inject() (repository: UserRepository, scc: SecuredControllerComponents)
  extends SecuredController(scc) {

  private val JWT_ID = "user"
  
  def login = Action(parse.json).async { implicit req =>
    Logger.info("Logging in")
    (for {
      user <- parsePayload(req)
      // parse payload ignores optional parameters: they need to 
      // be processed separately if they are needed to complete to action
      password <- Future{ user.password.get } failMessage "Missing password" 
      maybeUser <- repository.getUserByCredentials(user.username, password)
      httpResponse <- loginResponse(maybeUser) // // passes an api user (no sensitive info) to the response
    } yield (httpResponse)).logFailure.handleRecover
  }

  def register = Action(parse.json).async { implicit req =>
    Logger.info("Registering user")
    (for {
      user <- parsePayload(req)
      email <- Future { user.email.get } failMessage "Missing email"
      _ <- verifyUserAlreadyRegistered(email)
      id <- repository.addOne(user.copy(_id = newId))
      httpResponse <- registerResponse(id, user.username) // passes an api user (no sensitive info) to the response
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  def logout = Action.async { implicit req =>
    Logger.info("Logging out")
    Future { Ok.removingFromSession(JWT_ID) } // safe op: no failure no recovery
  }

  def verifyToken = Action.async { req =>
    Logger.info(s"Verifying token (claims: ${req.jwtSession.claimData.toString})") 
    Future {
      req.jwtSession.apply(JWT_ID)
        .map(_ => Ok)
        .getOrElse(Unauthorized("Token expired")) // safe op: no failure no recovery
    }
  }

  // TODO change this to email...
  def verifyUsernameAlreadyTaken = Action(parse.text).async { implicit req =>
    Logger.info("Finding matching username")
    (for {
      (username, users) <- parseText zip repository.getAll 
      maybeUser <- Future { users.find(_.username == username) }
      httpResponse <- Future { Ok(toJson(maybeUser.size)) }
    } yield(httpResponse)).logFailure.handleRecover
  }
  
  def verifyUserAlreadyRegistered(email: String) = {
    for {
      maybeUser <- repository.getByEmail(email)
      error <- Future { maybeUser.map(_ => throw new Exception("Email address already registered")) }
    }
    yield(error)
  }

  private def loginResponse(maybeUser: Option[User])(implicit req: Request[JsValue]) = {
    Future {
      maybeUser
        // safe to call get since the user comes from the DB
        .map(user => Ok(user._id.get).addingToJwtSession(JWT_ID, toJson(createApiUser(user))))
        .getOrElse(Unauthorized)
    }
  }
  private def registerResponse(id: String, username: String)(implicit req: Request[JsValue]) = {
    Future.successful{ Ok(id).addingToJwtSession(JWT_ID, toJson(createApiUser(id, username))) }
  }
  private def createApiUser(user: User) = User(_id = user._id, username = user.username)
  private def createApiUser(id: String, username: String) = User(_id = Some(id), username = username)
  
  
  
  
  
  
  
  
  
  
  
  
  // TO be FIXed or removeD
  def getAllUsernames = Action.async {
    Logger.info("Fetching usernames")
    repository.getAll
    .map(all => Ok(toJson(all.map(_.username)))).logFailure.handleRecover
  }

  def getAll = AuthenticatedAction.async {
    Logger.info("Fetching users")
    repository.getAll.map(all => Ok(toJson(all.map(x => User(_id = x._id, username = x.username)))))
  }

  def deleteUser(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting user")

    //TODO parse id
    repository.deleteOne(id).map({
      case Some(user) => Ok(toJson(User(_id = user._id, username = user.username)))
      case None       => NotFound
    })
  }

  def updateUser(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating user")

    //TODO parse id
    val updated = req.body.validate[User].get
    repository.updateOne(id, updated).map({
      case Some(user) => Ok(toJson(User(_id = user._id, username = user.username)))
      case None       => NotFound
    })
  }
}