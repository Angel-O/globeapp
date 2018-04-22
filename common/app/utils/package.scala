import scala.concurrent.Future
import scala.concurrent.duration._
import reactivemongo.bson.BSONObjectID
import java.time._
import scala.concurrent.ExecutionContext
import pdi.jwt.JwtSession._
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
import play.api.libs.ws._
import play.api.mvc.AnyContent
import scala.util.Try
import com.github.dwickern.macros.NameOf
import com.github.dwickern.macros.NameOf._
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Configuration
import config.AppConfig

//import com.google.inject.Inject

package object utils {
 
  val nameOf = NameOf
  
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
    
    def parseText(implicit ec: ExecutionContext, req: Request[String]): Future[String] = {
      Future.successful { req.body } 
    }
    
    //The data from this type of response comes from db...no need to validate... server 2 server communication...
    def parseResponse[T <: Entity](res: WSResponse)(implicit read: Reads[T], ec: ExecutionContext) = {
      Future { if (res.status == 200) res.json.validate[T].get else handleResponseError(res) }
    }
    
    def parseResponseAll[T <: Entity](res: WSResponse)(implicit read: Reads[T], ec: ExecutionContext) = {
      Future { if (res.status == 200) res.json.validate[Seq[T]].get else handleResponseError(res) } 
    }
    
    // this will be returned to the client, no need to add anything apart from status text
    private def handleResponseError(response: WSResponse) = response.status match {
      case 401 => throw UnauthorizedException(response.statusText) // Should not be happening
      case 403 => throw ForbiddenException(response.statusText) 
      case 404 => throw NotFoundException(response.statusText)
      case _ => throw new Exception(response.statusText)
    }
  }
  
  object FutureImplicits {
    implicit class ErrorMessageFuture[A](x: Future[A]){
      def failMessage(message: String)(implicit ec: ExecutionContext) = 
        x.recoverWith({case ex => Future.failed(new Exception(message, ex))})
    }
    
    implicit class LogErrorFuture[A](x: Future[A]){
      def logFailure(implicit ec: ExecutionContext) =
        x.andThen({ case Failure(ex) => Logger.error(ex.getMessage, ex.getCause) })
    }
    
    implicit class LogResponseFuture(x: Future[WSResponse]){
      def logResponseError(implicit ec:ExecutionContext) = 
        x.andThen({
          case maybeResponse: Try[WSResponse] =>
            maybeResponse.map(response => response.header("error").map(err => Logger.error(err)))
        })
    }

    // TODO investigate: why can't the andThen be extracted into
    // it's own implicit class like LogErrorFuture??
    // TODO avoid returning the error to the ui client maybe add error to the session
    implicit class RecoveryFuture(x: Future[Result]) {
      def handleRecover(implicit ec: ExecutionContext) =
        x.recover({
          case UnauthorizedException(msg) => Unauthorized.withHeaders(("error" -> msg)) 
          case ForbiddenException(msg)    => Forbidden.withHeaders(("error" -> msg))
          case NotFoundException(msg)     => NotFound.withHeaders(("error" -> msg))
          case _                          => BadRequest
        })
    }
  }
  
  object ApiClient {
    import FutureImplicits._
    def Get(url: String)(implicit ws: WSClient, req: Request[_], ec:ExecutionContext, config: AppConfig): Future[WSResponse] = {
      val token = req.jwtSession.serialize
      val apiRequest: WSRequest = ws.url(url)
      import config.Api._
      apiRequest
        .addHttpHeaders("Accept" -> s"$CONTENT")
        .addHttpHeaders(s"$TOKEN_HEADER" -> token)
        .withRequestTimeout(REQUEST_TIMEOUT.millis)
        .get()
        .logResponseError      
    }
  }
}