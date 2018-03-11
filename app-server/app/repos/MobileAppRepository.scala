package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json._
import play.api.libs.json.Json
import reactivemongo.bson.{ BSONDocument, BSONObjectID }
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import upickle.default._
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader
//import models.MobileApp
import apimodels.mobileapp.MobileApp
import play.api.Logger

class MobileAppRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {

  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("mobile-apps"))

  def getAll: Future[Seq[MobileApp]] = {
    val query = obj()
    entityCollection.flatMap(_.find(query)
      .cursor[MobileApp](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[MobileApp]]()))
  }

  def addApp(app: MobileApp): Future[WriteResult] = {
    Future.fromTry(BSONObjectID.parse(app._id))
      .flatMap(_ => entityCollection.flatMap(_.insert(app)))
      .recover({ case ex => Logger.error(ex.getMessage); throw ex })
  }

  //TODO shall I parse the id??
  def getApp(id: String): Future[Option[MobileApp]] = {
    val query = obj("_id" -> id)
    entityCollection.flatMap(_.find(query).one[MobileApp])
  }

  def updateApp(id: String, updated: MobileApp): Future[Option[MobileApp]] = {
    val selector = obj("_id" -> id)
    entityCollection
      .flatMap(_.findAndUpdate(selector, updated, fetchNewObject = true)
        .map(_.result[MobileApp]))
  }

  def deleteApp(id: String): Future[Option[MobileApp]] = {
    val selector = obj("_id" -> id)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[MobileApp]))
  }
}