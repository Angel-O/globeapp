package repository

import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.api.libs.json.Json.obj
import reactivemongo.bson.BSONObjectID
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import play.api.libs.json.Reads
import play.api.libs.json.OWrites
import play.api.libs.json.OFormat
import apimodels.common.Entity
import com.github.dwickern.macros.NameOf._

trait SearchCriteria {
  def byId(_id: String) = obj(nameOf(_id) -> _id)
  def any = obj()
}

abstract class GenericRepository[T <: Entity](collectionName: String, ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) extends SearchCriteria {

  private implicit val executionContext = ec
  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection(collectionName))

  def getAll(implicit read: Reads[T]): Future[Seq[T]] = {
    entityCollection.flatMap(_.find(any)
      .cursor[T](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[T]]()))
  }

  def addOne(item: T)(implicit writes: OWrites[T]): Future[String] = {
    entityCollection.flatMap(_.insert(item).map(_ => item._id.get))
  }

  def getById(id: String)(implicit read: Reads[T]): Future[Option[T]] = {
    entityCollection.flatMap(_.find(byId(id)).one[T])
  }

  def updateOne(id: String, updated: T)(implicit write: OFormat[T]): Future[Option[T]] = {
    entityCollection.flatMap(_.findAndUpdate(byId(id), updated, fetchNewObject = true).map(_.result[T]))
  }

  def deleteOne(id: String)(implicit read: Reads[T]): Future[Option[T]] = {
    entityCollection.flatMap(_.findAndRemove(byId(id)).map(_.result[T]))
  }

  def findOneBy(criteria: JsObject)(implicit read: Reads[T]) = {
    entityCollection.flatMap(_.find(criteria).one[T])
  }

  def findManyBy(criteria: JsObject)(implicit read: Reads[T]): Future[Seq[T]] = {
    entityCollection.flatMap(_.find(criteria)
      .cursor[T](ReadPreference.primary)
      .collect(-1, Cursor.FailOnError[Seq[T]]()))
  }
}