package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.mobileapp.MobileApp
import javax.inject.Inject
import play.api.Logger
import reactivemongo.bson.BSONObjectID
import repos.MobileAppRepository
import upickle.default.write

class MobileAppController @Inject() (
  scc:        SecuredControllerComponents,
  repository: MobileAppRepository)
  extends SecuredController(scc) {

  def getAll = Action.async {
    Logger.info("Fetching mobile apps")
    repository.getAll.map(all => Ok(write(all)))
  }

  def getApp(id: String) = Action.async {
    Logger.info("Fetching mobile app")
    parseId(id)
      .flatMap(validId => repository.getApp(validId).map({
        case Some(mobileApp) => Ok(write(mobileApp))
        case None            => NotFound
      }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def postApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating mobile app")
    req.body.validate[MobileApp].map(app =>
      parseId(app._id).flatMap(validId =>
        repository.addApp(app)
          .map(_ => Created(validId))
          .recover({ case ex => Logger.error(ex.getMessage); BadRequest })))
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }

  def deleteApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    parseId(id).flatMap(validId => repository
      .deleteApp(validId)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp))
        case None            => NotFound
      }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def updateApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    req.body.validate[MobileApp].map(app =>
      parseId(id).flatMap(validId => repository
        .updateApp(validId, app)
        .map({
          case Some(mobileApp) => Ok(write(mobileApp))
          case None            => NotFound
        }))
        .recover({ case ex => Logger.error(ex.getMessage); BadRequest }))
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }

  private def parseId(id: String) = {
    Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
  }
}
