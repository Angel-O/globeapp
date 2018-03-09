package controllers
import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.functional.syntax._
import apimodels.user.{User, LoginDetails, RegistrationDetails}
import pdi.jwt.JwtSession._
import upickle.default._
import scala.concurrent.ExecutionContext.Implicits.global
import repos.UserRepository
import models.RegisteredUser
import play.api.Logger
import reactivemongo.bson.BSONObjectID
import pdi.jwt.JwtSession

class ApplicationController @Inject()(repository: UserRepository, scc: SecuredControllerComponents) 
extends SecuredController(scc){
 
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
  
  def verifyToken = Action.async { req =>
    Logger.info("Verifying token")
    //Logger.info(Json.stringify(req.jwtSession("user").get))
    
    Logger.info(req.jwtSession.claimData.toString)
    val result = req.jwtSession.apply("user") match { //req.jwtSession.apply("user") match {
      case Some(json) => {
        //val user = read[User](json.as[String])
        //if (user != null) Ok else Unauthorized
        Logger.info("All good")
        Ok
      }
      case None => Unauthorized
    }
    
    Future{result}
    
    
    //Logger.info(Json.stringify(req.jwtSession.claimData))
    //val u = read[User](Json.stringify(req.jwtSession.claimData("user")))
//    if(user != null) Future{Ok}
//    else Future{Unauthorized}
//    val userOption = req.jwtSession.claimData("user").asOpt[User] //TODO use upickle for consistency
//    //Logger.info(userOption.get.username)
//    val result = userOption match {
//      case Some(_) => Ok
//      case None => Unauthorized
//    }
//   
//    Future{result}}
  }
  
  def getAllUsernames = Action.async { 
    Logger.info("Fetching usernames")
    repository.getAll.map(all => Ok(write(all.map(_.username)))) 
  }
  
  def verifyUsernameAlreadyTaken = Action(parse.text).async { req =>
    Logger.info("Finding matching username")
    val username = req.body
    repository.getAll.map(users => Ok(write(users.find(_.username == username).map(_ => 1).getOrElse(0)))) 
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
     Logger.info("Deleting user")
     //val id: String = req.body.as[String]
     val id = req.user.id //TODO this id belongs to the logged in user...self deletion, remove parse json if not used
     repository.deleteUser(BSONObjectID.parse(id).get).map({
       case Some(user) => Ok(write(User(user._id.stringify, user.username)))
       case None => NotFound
     })
  }

  def updateUser = AuthenticatedAction.async(parse.json) { req =>
      Logger.info("Updating user")
      //val payload: String = Json.stringify(req.body)
      val id = req.user.id
      val updated = req.body.validate[RegisteredUser].get //read[User](payload)
      repository.updateUser(BSONObjectID.parse(id).get, updated).map({
        case Some(user) => Ok(write(User(user._id.stringify, user.name)))
        case None => NotFound
      })
  }
}