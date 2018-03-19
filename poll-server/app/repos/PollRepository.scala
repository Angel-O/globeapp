package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json._
import play.api.libs.json.Json.obj
import reactivemongo.bson.BSONObjectID
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import apimodels.poll.Poll
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader

class PollRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {

  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("polls"))

  def getAll: Future[Seq[Poll]] = {
    val query = obj()
    entityCollection.flatMap(_.find(query)
      .cursor[Poll](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[Poll]]()))
  }

  def addPoll(poll: Poll): Future[WriteResult] = {
    entityCollection.flatMap(_.insert(poll))
  }

  def getPoll(id: String): Future[Option[Poll]] = {
    val query = obj("_id" -> id)
    entityCollection.flatMap(_.find(query).one[Poll])
  }

  def updatePoll(id: String, updated: Poll): Future[Option[Poll]] = {
    val selector = obj("_id" -> id)
    entityCollection.flatMap(_.findAndUpdate(selector, updated, fetchNewObject = true).map(_.result[Poll]))
  }

  def deletePoll(id: String): Future[Option[Poll]] = {
    val selector = obj("_id" -> id)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[Poll]))
  }
}