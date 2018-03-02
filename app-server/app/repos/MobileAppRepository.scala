package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import upickle.default._
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader
import models.MobileApp


class MobileAppRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){
  
  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("mobile-apps"))
  
  def getAll: Future[Seq[MobileApp]] = {
    val query = Json.obj()
    entityCollection.flatMap(_.find(query)
        .cursor[MobileApp](ReadPreference.primary)
        .collect(-1, Cursor.FailOnError[Seq[MobileApp]]()))
  }
  
  def addApp(app: MobileApp): Future[WriteResult] = {
    entityCollection.flatMap(_.insert(app))
  }
  
  def getApp(id: BSONObjectID): Future[Option[MobileApp]] = {
    val query = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.find(query).one[MobileApp])
  }

  def updateApp(id: BSONObjectID, updated: MobileApp): Future[Option[MobileApp]] = {
    val selector = BSONDocument("_id" -> id)
    val updateModifier = BSONDocument(
      "$set" -> BSONDocument(
        "name" -> updated.name)) //TODO add all fields...
    entityCollection.flatMap(_.findAndUpdate(selector, updateModifier, fetchNewObject = true).map(_.result[MobileApp]))
  }

  def deleteApp(id: BSONObjectID): Future[Option[MobileApp]] = {
    val selector = BSONDocument("_id" -> id)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[MobileApp]))
  }
}