package controllers
import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._
import apimodels.user.User
import pdi.jwt.JwtSession._
import scala.concurrent.ExecutionContext.Implicits.global
import repos.UserRepository
import play.api.Logger
import play.api.libs.json.Json._
import reactivemongo.bson.BSONObjectID
import pdi.jwt.JwtSession

class AuthenticationController @Inject() (repository: UserRepository, scc: SecuredControllerComponents)
  extends SecuredController(scc) {

  def login = Action(parse.json).async { implicit request: Request[JsValue] =>
    Logger.info("Logging in")
    val result = request.body
      .validate[User]
      .fold(
        errors => Future { BadRequest(JsError.toJson(errors)) },
        {
          case User(_, _, username, password, _, _) =>
            repository.getAll.map(_.find(_.username == username))
              .map({
                case Some(user) => user.password == password match {
                  case true => {
                    val apiUser = User(_id = user._id, username = user.username)
                    Ok.addingToJwtSession("user", toJson(apiUser))
                  }
                  case false => Unauthorized
                }
                case None => Unauthorized
              })
        })
    result
  }

  def logout = Action.async { implicit req =>
    Logger.info("Logging out")
    Future { Ok.removingFromSession("user") }
  }

  def register = Action(parse.json).async { implicit request: Request[JsValue] =>
    Logger.info("Registering user")
    val result = request.body
      .validate[User]
      .map({
        case user @ User(_, _, username, email, _, _) => { //TODO check if user already exists... (use email maybe...)
          val id = newId
          repository.addOne(user.copy(_id = newId))
          val apiUser = User(_id = id, username = username)
          Ok.addingToJwtSession("user", toJson(apiUser))
        }
      })
      .recover({ case ex => BadRequest(JsError.toJson(ex.errors)) })

    Future { result.get }
  }

  def verifyToken = Action.async { req =>
    Logger.info("Verifying token")

    Logger.info(req.jwtSession.claimData.toString)
    val result = req.jwtSession.apply("user") match {
      case Some(json) => {
        Logger.info("All good")
        Ok
      }
      case None => Unauthorized
    }

    Future { result }
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

  private def parseId(id: String) = {
    Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
  }

  private def newId = Some(BSONObjectID.generate.stringify)
}