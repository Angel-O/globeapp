package controllers
import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._
import apimodels.User
import pdi.jwt.JwtSession._
import upickle.default._
import scala.concurrent.ExecutionContext.Implicits.global
import repos.UserRepository
import models.RegisteredUser
import play.api.Logger
import reactivemongo.bson.BSONObjectID
import pdi.jwt.JwtSession

// overkill
sealed trait Gender
case object Male extends Gender
case object Female extends Gender
case object Gender {
  import scala.language.implicitConversions
  import JsonFormats.GenderFormat.writes
  implicit def asString(gender: Gender): String = writes(gender).as[String]
}

case object JsonFormats{
  implicit val loginFormat: OFormat[LoginDetails] = Json.format[LoginDetails]
  implicit val registrationFormat: OFormat[RegistrationDetails] = Json.format[RegistrationDetails]
  implicit object GenderFormat extends Format[Gender] {
    def reads(json: JsValue): JsResult[Gender] = json.as[String].toLowerCase() match{
      case "male" => JsSuccess(Male)
      case "female" => JsSuccess(Female)
      case _ => JsError("Invalid Gender")
    } 
    def writes(gender: Gender) = gender match {
      case Male => JsString("male")
      case Female => JsString("female")
    }   
  }
}

case class LoginDetails(username: String, password: String)
case class RegistrationDetails(name: String, username: String, email: String, password: String, gender: Gender)

class ApplicationController @Inject()(repository: UserRepository, scc: SecuredControllerComponents) 
extends SecuredController(scc){
  import JsonFormats._
  
//  val loginDetails: Reads[(String, String)] = (
//      (JsPath \ "username").read[String] and
//      (JsPath \ "password").read[String]).tupled
    
//  val registrationDetails: Reads[(String, String, String, String, Gender)] = (
//      (JsPath \ "name").read[String] and
//      (JsPath \ "username").read[String] and
//      (JsPath \ "email").read[String] and
//      (JsPath \ "password").read[String] and
//      (JsPath \ "gender").read[Gender]).tupled
   
  
  def login = Action(parse.json).async { implicit request: Request[JsValue] =>
    Logger.info("Logging in")
    val result = request.body
      .validate[LoginDetails]
      .fold( 
          errors => Future{ BadRequest(JsError.toJson(errors)) }, 
          { case LoginDetails(username, password) =>
              repository.getAll.map(_.find(_.username == username))
              .map({ 
                case Some(registeredUser) => registeredUser.password == password match {
                  case true => {
                    val apiUser = User(registeredUser._id.stringify, registeredUser.username)
                    Ok.addingToJwtSession("user", write(apiUser)) 
                    //val jwtSession = JwtSession() + ("user", write(apiUser))
                    //val token = jwtSession.serialize
                    //Ok.withHeaders(("Token", token))
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
//    val json = req.jwtSession.apply("user").get
//    val user = read[User](json.as[String])
//    println("username", user.username)
//    val result = Ok.withNewJwtSession
//    val session = Ok.jwtSession - ("user")
    Future{Ok.removingFromSession("user")}
  }
  
  def register = Action(parse.json).async { implicit request: Request[JsValue] =>
    Logger.info("Registering user")
    val result = request.body
      .validate[RegistrationDetails]
      .map({ case RegistrationDetails(name, username, email, password, gender) => { //TODO check if user already exists...
                val registeredUser = RegisteredUser(name, username, email, password, gender)
                repository.addUser(registeredUser) 
                val apiUser = User(registeredUser._id.stringify, username) //TODO create id case class 
                Ok.addingToJwtSession("user", write(apiUser))
             }
           })
      .recover({ case ex => BadRequest(JsError.toJson(ex.errors)) })
      
    Future{result.get}
  }
  
  def getAllUsernames = Action.async { 
    Logger.info("Fetching usernames")
    repository.getAll.map(all => Ok(write(all.map(_.username)))) 
  }
  
  def getAll = AuthenticatedAction.async { 
    Logger.info("Fetching users")
    repository.getAll.map(all => Ok(write(all.map(x => User(x._id.stringify, x.username))))) 
  }
  
//  def postUser = Action.async(parse.json) { req =>   
//     val payload: String = Json.stringify(req.body)
//     val user = read[User](payload)
//     repository.addUser(user).map(_ => Created)
//  }

  def deleteUser = AuthenticatedAction.async(parse.json) { req => 
     Logger.info("Deleting users")
     //val id: String = req.body.as[String]
     val id = req.user.id //TODO this id belongs to the logged in user...self deletion, remove parse json if not used
     repository.deleteUser(BSONObjectID.parse(id).get).map({
       case Some(user) => Ok(write(User(user._id.stringify, user.username)))
       case None => NotFound
     })
  }

  def updateUser = AuthenticatedAction.async(parse.json) { req =>
      Logger.info("Updating users")
      //val payload: String = Json.stringify(req.body)
      val id = req.user.id
      val updated = req.body.validate[RegisteredUser].get //read[User](payload)
      repository.updateUser(BSONObjectID.parse(id).get, updated).map({
        case Some(user) => Ok(write(User(user._id.stringify, user.name)))
        case None => NotFound
      })
  }
}