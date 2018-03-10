package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import reactivemongo.bson.{ BSONDocument, BSONObjectID }
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import upickle.default._
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader
import models.Poll

class PollRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {

  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("polls"))

  def getAll: Future[Seq[Poll]] = {
    val query = Json.obj()
    entityCollection.flatMap(_.find(query)
      .cursor[Poll](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[Poll]]()))
  }

  def addPoll(poll: Poll): Future[WriteResult] = {
    entityCollection.flatMap(_.insert(poll))
  }

  def getPoll(id: BSONObjectID): Future[Option[Poll]] = {
    val query = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.find(query).one[Poll])
  }

  def updatePoll(id: BSONObjectID, updated: Poll): Future[Option[Poll]] = {
    val selector = BSONDocument("_id" -> id)
    val updateModifier = BSONDocument(
      "$set" -> BSONDocument(
        "options" -> write(updated.options))) //TODO add all fields...note write(it's not upickle)
    entityCollection.flatMap(_.findAndUpdate(selector, updateModifier, fetchNewObject = true).map(_.result[Poll]))
  }

  def deletePoll(id: BSONObjectID): Future[Option[Poll]] = {
    val selector = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[Poll]))
  }
}