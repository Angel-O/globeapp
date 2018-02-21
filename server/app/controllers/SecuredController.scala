package controllers

import javax.inject.Inject

import apimodels.User
//import pdi.jwt.JwtUpickle._ 
import pdi.jwt.JwtSession._
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.mvc.Results._
import play.api.mvc._
import upickle.default._

import scala.concurrent.{ ExecutionContext, Future }

class AuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedActionBuilder @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
    extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    //(request.jwtSession.getAs[String]("user").get) match {
    request.headers.get("user") match {
      case Some(json) => {
        val user = read[User](json)
        block(new AuthenticatedRequest[A](user, request)).map(_.refreshJwtSession(request))
      }     
      case _ =>
        Future(Unauthorized)
    }
//    request.jwtSession.getAs[User]("user") match {
//      case Some(user) =>
//        block(new AuthenticatedRequest[A](user, request)).map(_.refreshJwtSession(request))
//      case _ =>
//        Future(Unauthorized)
//    }
  }
}

//import play.api.Logger
//
//case class Logging[A](action: Action[A]) extends Action[A] {
//  def apply(request: Request[A]): Future[Result] = {
//    Logger.info("Calling action")
//    action(request)
//  }
//
//  override def parser = action.parser
//  override def executionContext = action.executionContext
//}
//
//class LoggingAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
//  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
//    block(request)
//  }
//  override def composeAction[A](action: Action[A]) = new Logging(action)
//}

//class AdminActionBuilder @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
//    extends ActionBuilderImpl(parser) {
//  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
//    read[User](request.headers("user")) match {
//      case user @ User(_, _) if user.isAdmin =>
//        block(new AuthenticatedRequest(user, request)).map(_.refreshJwtSession(request))
//      case Some(_) =>
//        Future(Forbidden.refreshJwtSession(request))
//      case _ =>
//        Future(Unauthorized)
//    }
////    request.jwtSession.getAs[User]("user") match {
////      case Some(user) if user.isAdmin =>
////        block(new AuthenticatedRequest(user, request)).map(_.refreshJwtSession(request))
////      case Some(_) =>
////        Future(Forbidden.refreshJwtSession(request))
////      case _ =>
////        Future(Unauthorized)
////    }
//  }
//}

case class SecuredControllerComponents @Inject()(
    //adminActionBuilder: AdminActionBuilder,
    authenticatedActionBuilder: AuthenticatedActionBuilder,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents

class SecuredController @Inject()(scc: SecuredControllerComponents) extends AbstractController(scc) {
  //def AdminAction: AdminActionBuilder                 = scc.adminActionBuilder
  def AuthenticatedAction: AuthenticatedActionBuilder = scc.authenticatedActionBuilder
}