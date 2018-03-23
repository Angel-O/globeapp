import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import java.time._
import scala.concurrent.ExecutionContext
import play.api.mvc.Request
import play.api.libs.json.JsValue
import play.api.libs.json.Reads

package object utils {

  object Bson {
    def parseId(id: String) = {
      Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
    }

    def newId = Some(BSONObjectID.generate.stringify)
  }
  
  object Date {
    def newDate = Some(LocalDate.now())
  }
  
  object Json {
    import FutureImplicits._
    def parsePayload[T](req: Request[JsValue])(implicit read: Reads[T], ec: ExecutionContext) = {
      Future { req.body.validate[T].get } failMessage "Invalid payload"
    }
  }
  
  object FutureImplicits {
    implicit class ErrorMessageFuture[A](x: Future[A]){
      def failMessage(message: String)(implicit ec: ExecutionContext) = 
        x.recoverWith({case ex => Future.failed(new Exception(message))})
    }
  }
}