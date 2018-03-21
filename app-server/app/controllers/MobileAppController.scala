package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.mobile.MobileApp
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Json._
import repos.MobileAppRepository
import utils.Bson._

class MobileAppController @Inject() (
  scc:        SecuredControllerComponents,
  repository: MobileAppRepository)
  extends SecuredController(scc) {

  def getAll = Action.async {
    Logger.info("Fetching mobile apps")
    repository.getAll.map(all => Ok(toJson(all)))
  }

  def getApp(id: String) = Action.async {
    Logger.info("Fetching mobile app")
    parseId(id)
      .flatMap(validId =>
        repository
          .getById(validId))
          .map({
            case Some(mobileApp) => Ok(toJson(mobileApp))
            case None            => NotFound
          })
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def postApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating mobile app")
    req.body.validate[MobileApp]
      .map(uploadModel => {
        repository
          .getByKey(uploadModel.name, uploadModel.company, uploadModel.store)
          .flatMap({
            case Some(_) =>
              Future(BadRequest(
                  "Found existing app with same name, company, store combination "+ 
                  s"(${uploadModel.name}, ${uploadModel.company}, ${uploadModel.store})"))
            case None => {
              val app = uploadModel.copy(_id = newId)
              repository
                .addOne(app)
                .map(id => Created(id))
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
          .deleteOne(validId)
          .map({
            case Some(mobileApp) => Ok(toJson(mobileApp))
            case None            => NotFound
          }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def updateApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    req.body.validate[MobileApp]
      .map(uploadModel =>
        parseId(id)
          .flatMap(validId =>
            repository
              .updateOne(validId, uploadModel)
              .map({
                case Some(mobileApp) => Ok(toJson(mobileApp))
                case None            => NotFound
              }))
          .recover({ case ex => Logger.error(ex.getMessage); BadRequest }))
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }
}
