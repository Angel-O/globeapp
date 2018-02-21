package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import apimodels.{User => ApiUser}
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import upickle.default._
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader
import models.{RegisteredUser => User}


class UserRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){
  
  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("registered-users"))
  
  def getAll: Future[Seq[User]] = {
    val query = Json.obj()
    entityCollection.flatMap(_.find(query)
        .cursor[User](ReadPreference.primary)
        .collect(-1, Cursor.FailOnError[Seq[User]]()))
  }
  
  def addUser(user: User): Future[WriteResult] = {
    entityCollection.flatMap(_.insert(user))
  }
  
  def getUser(id: BSONObjectID): Future[Option[User]] = {
    val query = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.find(query).one[User])
  }

  def updateUser(id: BSONObjectID, updated: User): Future[Option[User]] = {
    val selector = BSONDocument("_id" -> id)
    val updateModifier = BSONDocument(
      "$set" -> BSONDocument(
        "name" -> updated.name)) //TODO add all fields...
    entityCollection.flatMap(_.findAndUpdate(selector, updateModifier, fetchNewObject = true).map(_.result[User]))
  }

  def deleteUser(id: BSONObjectID): Future[Option[User]] = {
    val selector = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[User]))
  }
}