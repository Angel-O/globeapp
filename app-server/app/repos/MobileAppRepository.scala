package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import apimodels.mobileapp.MobileApp
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.ReadPreference
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

class MobileAppRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {

  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("mobile-apps"))

  def getAll: Future[Seq[MobileApp]] = {
    val query = obj()
    entityCollection.flatMap(_.find(query)
      .cursor[MobileApp](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[MobileApp]]()))
  }

  def addApp(app: MobileApp): Future[WriteResult] = {
    entityCollection.flatMap(_.insert(app))
  }

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
  
  def validateUniqueApp(name: String, company: String, store: String): Future[Option[MobileApp]] = {
    val query = obj("name" -> name, "company" -> company, "store" -> store)
    entityCollection.flatMap(_.find(query).one[MobileApp])
  }
}