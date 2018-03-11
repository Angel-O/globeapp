package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.mobileapp.MobileApp
import apimodels.mobileapp.MobileAppUploadModel
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
      .flatMap(validId =>
        repository
          .getApp(validId)
          .map({
            case Some(mobileApp) => Ok(write(mobileApp))
            case None            => NotFound
          }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  //TODO create a model only to update the app...you don't want to 
  // update the same fields used when creating the app (e.g the 
  // store shouldn't be updated...)
  def postApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating mobile app")
    req.body.validate[MobileAppUploadModel]
      .map(uploadModel => {
        repository
          .validateUniqueApp(uploadModel.name, uploadModel.company, uploadModel.store)
          .flatMap({
            case Some(_) =>
              Future(BadRequest(
                  "Found existing app with same name, company, store combination "+ 
                  s"(${uploadModel.name}, ${uploadModel.company}, ${uploadModel.store})"))
            case None => {
              val app = createMobileApp(uploadModel)
              repository
                .addApp(app)
                .map(_ => Created(app._id))
                .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
            }
          })
      })
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }

  def deleteApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    parseId(id)
      .flatMap(validId =>
        repository
          .deleteApp(validId)
          .map({
            case Some(mobileApp) => Ok(write(mobileApp))
            case None            => NotFound
          }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def updateApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    req.body.validate[MobileAppUploadModel]
      .map(uploadModel =>
        parseId(id)
          .flatMap(validId =>
            repository
              .updateApp(validId, createMobileApp(uploadModel, Some(validId)))
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

  private def createMobileApp(uploadModel: MobileAppUploadModel, existingId: Option[String] = None) = {
    MobileApp(
      existingId.fold(BSONObjectID.generate.stringify)(identity),
      uploadModel.name,
      uploadModel.company,
      uploadModel.genre,
      uploadModel.price,
      uploadModel.store)
  }
}
