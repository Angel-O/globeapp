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
import play.api.libs.json.JsObject

object SearchCriteria{
  def uniqueApp(name: String, company: String, store: String) = 
    obj("name" -> name, "company" -> company, "store" -> store)
    
  def id(id: String) = obj("_id" -> id)
  
  def any = obj()
}

class MobileAppRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {

  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("mobile-apps"))

  def getAll: Future[Seq[MobileApp]] = findManyBy(SearchCriteria.any)

  def addApp(app: MobileApp): Future[String] = {
    entityCollection.flatMap(_.insert(app).map(_ => app._id.get))
  }

  def updateApp(id: String, updated: MobileApp): Future[Option[MobileApp]] = {
    entityCollection
      .flatMap(_.findAndUpdate(SearchCriteria.id(id), updated, fetchNewObject = true)
        .map(_.result[MobileApp]))
  }

  def deleteApp(id: String): Future[Option[MobileApp]] = {
    entityCollection.flatMap(_.findAndRemove(SearchCriteria.id(id)).map(_.result[MobileApp]))
  }
  
  def findOneBy(criteria: JsObject) = {
    entityCollection.flatMap(_.find(criteria).one[MobileApp])
  }
  
  def findManyBy(criteria: JsObject): Future[Seq[MobileApp]] = {
    entityCollection.flatMap(_.find(criteria)
      .cursor[MobileApp](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[MobileApp]]()))
  }
}