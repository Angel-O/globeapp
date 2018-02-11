package controllers

import javax.inject._
import play.api.mvc.ControllerComponents
import play.api.mvc.AbstractController
//import play.api.libs.json.Json
import apimodels.User
import upickle.default._

//import serialization.ReWr._


//object Serializer{
//  implicit val userReads = Json.reads[User]
//  implicit val userWrites = Json.writes[User]
//  implicit val userFormat = Json.format[User]
//}

@Singleton
class UserController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  
  //import Serializer._
  def getAll = Action {
    val users = getUsers
    //Ok(Json.toJson(users))
    Ok(write(users))
  }

  def getUsers() = {
    List(User("Angelo", 1), User("JohnD", 2), User("MikeR", 3), User("Paul", 3))
  }
}