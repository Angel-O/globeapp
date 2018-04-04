import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import java.time._
import scala.concurrent.ExecutionContext
import play.api.mvc.Results._
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import scala.util.Failure
import play.api.Logger
import scala.util.Success
import exceptions.ServerException._
import apimodels.common.Entity

package object utils {

  object Bson {
    import FutureImplicits._
    def parseId(id: String)(implicit ec: ExecutionContext) = {
      Future.fromTry(BSONObjectID.parse(id).map(_.stringify)) failMessage "Invalid id"
    }

    def newId = Some(BSONObjectID.generate.stringify)
  }
  
  object Date {
    def newDate = Some(LocalDate.now())
  }
  
  object Json {
    import FutureImplicits._
    def parsePayload[T <: Entity](req: Request[JsValue])(implicit read: Reads[T], ec: ExecutionContext) = {
      Future { req.body.validate[T].get } failMessage "Invalid payload"
    }
    
    def parseText[T <: Entity](implicit read: Reads[T], ec: ExecutionContext, req: Request[String]) = {
      Future.successful { req.body } 
    }
  }
  
  object FutureImplicits {
    implicit class ErrorMessageFuture[A](x: Future[A]){
      def failMessage(message: String)(implicit ec: ExecutionContext) = 
        x.recoverWith({case ex => Future.failed(new Exception(message, ex))})
    }
    
    implicit class LogErrorFuture[A](x: Future[A]){
      def logFailure(implicit ec: ExecutionContext) =
        x.andThen({ case Failure(ex) => Logger.error(ex.getMessage, ex) })
    }
    
    implicit class RecoveryFuture(x: Future[Result]){
      def handleRecover(implicit ec: ExecutionContext) =
        x.recover({ case ex: ForbiddenException => Forbidden case _ => BadRequest })
    }
  }
}