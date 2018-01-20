package controllers

import javax.inject._
import play.api.mvc.ControllerComponents
import play.api.mvc.AbstractController
import play.api.libs.json.Json

case object User{
  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]
  implicit val userFormat = Json.format[User]
}

case class User(name: String, id: Int)


@Singleton
class UserController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  
  def getAll = Action {
    val users = getUsers
    Ok(Json.toJson(users))
  }

  def getUsers() = {
    List(User("Angelo", 1), User("John", 2), User("Mike", 3), User("Paul", 3))
  }
}