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

object JsonFormats{
  //import play.api.libs.json.Reads._
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  implicit val userFormat: OFormat[User] = Json.format[User]
  
  implicit val userReads: Reads[User] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "_id").readNullable[BSONObjectID].map(x => Some(x.get.stringify))
    )(User.apply _)
    
   implicit val usertWrites: OWrites[User] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "_id").writeNullable[String]
    )(unlift(User.unapply))
    
//    implicit object UserReader extends BSONDocumentReader[User] {
//      def read(doc: BSONDocument) = User(     
//        doc.getAs[String]("name").get,
//        Some(doc.getAs[BSONObjectID]("_id").toString)
//        //doc.getAs[List[BSONObjectID]]("addresses").toList.flatten,
//      )
//    }
}

class UserRepository @Inject() (implicit ec: ExecutionContext, reactiveMongoApi: ReactiveMongoApi){
  
  import JsonFormats._
  def entityCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("users"))
  
  def getAll: Future[Seq[User]] = {
    val query = Json.obj()
    entityCollection.flatMap(_.find(query)
        .cursor[User](ReadPreference.primary)
        .collect(-1, Cursor.FailOnError[Seq[User]]()))
  }
  
  def addUser(user: User): Future[WriteResult] = {
    user._id match {
      case Some(id) => if(BSONObjectID.parse(id).isFailure) throw new IllegalArgumentException("invalid id") 
                       else entityCollection.flatMap(_.insert(user))
      case None => entityCollection.flatMap(_.insert(user))
    }
  }
  
  def getUser(id: String): Future[Option[User]] = {
    val query = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    entityCollection.flatMap(_.find(query).one[User])
  }

  def updateUser(id: String, updated: User): Future[Option[User]] = {
    val selector = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    val updateModifier = BSONDocument(
      "$set" -> BSONDocument(
        "name" -> updated.name))
    entityCollection.flatMap(_.findAndUpdate(selector, updateModifier, fetchNewObject = true).map(_.result[User]))
  }

  def deleteUser(id: String): Future[Option[User]] = {
    val selector = BSONDocument("_id" -> BSONObjectID.parse(id).get)
    entityCollection.flatMap(_.findAndRemove(selector).map(_.result[User]))
  }
}