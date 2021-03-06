package controllers

import javax.inject._
import play.api.mvc.ControllerComponents
import play.api.mvc.AbstractController
import play.api.libs.json.Json
import apimodels.User
import upickle.default._
import repos.UserRepository

import scala.concurrent.Future


//import serialization.ReWr._


//object Serializer{
//  implicit val userReads = Json.reads[User]
//  implicit val userWrites = Json.writes[User]
//  implicit val userFormat = Json.format[User]
//}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject() (cc: ControllerComponents, repository: UserRepository) extends AbstractController(cc) {
  def getAll = Action.async { 
    repository.getAll.map(all => Ok(write(all))) }
  
  def postUser = Action.async(parse.json) { req =>   
     val payload: String = Json.stringify(req.body)
     val user = read[User](payload)
     repository.addUser(user).map(_ => Created)
  }

  def deleteUser = Action.async(parse.json) { req => 
     val id: String = req.body.as[String]
     repository.deleteUser(id).map({
       case Some(user) => Ok(write(user))
       case None => NotFound
     })
  }

  def updateUser(id: String) = Action.async(parse.json) { req => 
      val payload: String = Json.stringify(req.body)
      val updated = read[User](payload)
      repository.updateUser(id, updated).map({
        case Some(user) => Ok(write(user))
        case None => NotFound
      })
  }
}