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


sealed trait Gender
case object Male extends Gender
case object Female extends Gender

case object JsonFormats{
  implicit val loginFormat: OFormat[LoginDetails] = Json.format[LoginDetails]
//  implicit object GenderReads extends Reads[Gender] {
//    override def reads(json: JsValue): JsResult[Gender] = json.as[String].toLowerCase() match{
//      case "male" => JsSuccess(Male)
//      case "female" => JsSuccess(Female)
//      case _ => JsError("Invalid Gender")
//    }
//  }
  implicit object GenderFormat extends OFormat[Gender] {
    def reads(json: JsValue): JsResult[Gender] = json.as[String].toLowerCase() match{
      case "male" => JsSuccess(Male)
      case "female" => JsSuccess(Female)
      case _ => JsError("Invalid Gender")
    }
    
    def writes(gender: Gender) = gender match {
      case Male => JsObject(Seq(("gender", Json.toJson("male"))))
      case Female => JsObject(Seq(("gender", Json.toJson("female"))))
    }
  }
  implicit val registrationFormat: OFormat[RegistrationDetails] = Json.format[RegistrationDetails]
}


case class LoginDetails(username: String, password: String)
case class RegistrationDetails(name: String, username: String, email: String, password: String, gender: Gender)

class ApplicationController @Inject()(repository: UserRepository, scc: SecuredControllerComponents) 
extends SecuredController(scc){
  import JsonFormats._
  
//  val loginDetails: Reads[(String, String)] = (
//      (JsPath \ "username").read[String] and
//      (JsPath \ "password").read[String]).tupled
    
  //TODO create a class rather than using tuples
  val registrationDetails: Reads[(String, String, String, String, Gender)] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "username").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "password").read[String] and
      (JsPath \ "gender").read[Gender]).tupled //TODO use sealed trait
   
  
  def login = Action(parse.json).async { implicit request: Request[JsValue] =>
    val result = request.body
      .validate[LoginDetails]
      .fold( 
          errors => Future{ BadRequest(JsError.toJson(errors)) }, 
          { case LoginDetails(username, password) =>
              repository.getAll.map(_.find(_.username == username))
              .map({ 
                case Some(registeredUser) => registeredUser.password == password match {
                  case true => {
                    val apiUser = User(registeredUser.name, Some(registeredUser._id.stringify))
                    Ok.addingToJwtSession("user", write(apiUser)) 
                  }
                  case false => Unauthorized
                }
                case None => Unauthorized 
               })
          })
     result
  }
  
  def register = Action(parse.json).async { implicit request: Request[JsValue] =>
    val result = request.body
      .validate[RegistrationDetails]
      .map({ case RegistrationDetails(name, username, email, password, gender) => { //TODO check if user already exists...
                //Logger.info(GenderFormat.writes(gender).toString)
                val registeredUser = RegisteredUser(name, username, email, password, gender.toString.toLowerCase)
                repository.addUser(registeredUser) 
                val apiUser = User(name, Some(registeredUser._id.stringify))
                Ok.addingToJwtSession("user", write(apiUser))
             }
           })
      .recover({ case ex => BadRequest(JsError.toJson(ex.errors)) })
      
    Future{result.get}
  }
}