package repos

import play.modules.reactivemongo.ReactiveMongoApi
import javax.inject.Inject
import scala.concurrent.Future
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.ExecutionContext
import apimodels.User
import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._

import upickle.default._
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocumentReader


object Entity{
  def apply(_id: Option[String], mm: String) = new Entity(_id, mm)
}
case class Entity(_id: Option[String] = None, mm: String)

object EntityJsonFormats{
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  implicit val entityFormat: OFormat[Entity] = Json.format[Entity]
  
  implicit val entityReads: Reads[Entity] = (
    (JsPath \ "_id").readNullable[BSONObjectID].map(x => Some(x.get.stringify)) and
    (JsPath \ "mm").read[String]
    )(Entity.apply _)
//    
//   implicit val entityWrites: OWrites[Entity] = (
//    (JsPath \ "_id").writeNullable[String]
//    )(unlift(Entity.unapply))
}

abstract class RepoBase[T<:Entity] (ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi) {
  implicit val exCo = ec
  import EntityJsonFormats._
  import JsonFormats._
  def entityCollection: Future[JSONCollection] //reactiveMongoApi.database.map(_.collection("users"))
  
  def getAll: Future[Seq[T]] = {
    val query = Json.obj()
    entityCollection.flatMap(_.find(query)
        .cursor[Entity](ReadPreference.primary)
        .collect(-1, Cursor.FailOnError[Seq[Entity]]())).map(_.map(_.asInstanceOf[T]))
  }
  
  def addUser(entity: Entity): Future[WriteResult] = {
    entity._id match {
      case Some(id) => if(BSONObjectID.parse(id).isFailure) throw new IllegalArgumentException("invalid id") 
                       else entityCollection.flatMap(_.insert(entity))
      case None => entityCollection.flatMap(_.insert(entity))
    }
  }
  
  def getUserById(id: String): Future[Option[Entity]] = {
    val query = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    entityCollection.flatMap(_.find(query).one[Entity])
  }

  def updateUser(id: String, user: Entity, update: BSONDocument): Future[Option[Entity]] = {
    val selector = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    
    entityCollection.flatMap(_.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[Entity]))
  }

  def deleteUser(id: String): Future[Option[Entity]] = {
    val selector = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[Entity]))
  }
}