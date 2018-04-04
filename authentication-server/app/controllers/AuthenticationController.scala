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

  def login = Action(parse.json).async { implicit req =>
    Logger.info("Logging in")
    (for {
      User(_, _, username, Some(password), _, _) <- parsePayload(req) // assuming pwd is provided!!!
      maybeUser <- repository.getApiUserByCredentials(username, password)
      httpResponse <- Future {
        maybeUser
          .map(user => Ok(user._id.get).addingToJwtSession("user", toJson(toApiUser(user))))
          .getOrElse(Unauthorized)
      }
    } yield (httpResponse)).logFailure.handleRecover
  }

  def logout = Action.async { implicit req =>
    Logger.info("Logging out")
    Future { Ok.removingFromSession("user") } // safe op: no failure no recovery
  }

  def register = Action(parse.json).async { implicit req =>
    Logger.info("Registering user")
    (for {
      user @ User(_, _, username, _, email, _) <- parsePayload(req) //TODO check if user already exists... (use email maybe...)
      id <- repository.addOne(user.copy(_id = newId))
      httpResponse <- Future.successful{ Ok(id).addingToJwtSession("user", toJson(toApiUser(id, username))) }
    } yield (httpResponse)).logFailure.handleRecover
  }

  def verifyToken = Action.async { req =>
    Logger.info("Verifying token")
    Logger.info(req.jwtSession.claimData.toString)
    
    Future {
      req.jwtSession.apply("user")
        .map(_ => Ok)
        .getOrElse(Unauthorized("Token expired")) // safe op: no failure no recovery
    }
  }

  def getAllUsernames = Action.async {
    Logger.info("Fetching usernames")
    repository.getAll.map(all => Ok(toJson(all.map(_.username))))
  }

  def verifyUsernameAlreadyTaken = Action(parse.text).async { req =>
    Logger.info("Finding matching username")
    val username = req.body
    repository.getAll.map(users => Ok(toJson(users.find(_.username == username).map(_ => 1).getOrElse(0))))
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
  
  private def toApiUser(user: User) = User(_id = user._id, username = user.username)
  private def toApiUser(id: String, username: String) = User(_id = Some(id), username = username)
}