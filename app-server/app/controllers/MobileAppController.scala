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
import utils.FutureImplicits._
import utils.Json._
import exceptions.ServerException._

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
    (for {
      validId <- parseId(id)
      maybeMobileApp <- repository.getById(validId)
      httpResponse <- Future.successful { maybeMobileApp.map(app => Ok(toJson(app))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.handleRecover
  }

  def postApp = AuthenticatedAction.async(parse.json) { req => 
    Logger.info("Creating mobile app")
    (for {
      validPayload <- parsePayload[MobileApp](req)
      _ <- verifyAppAlreadyRegistered(validPayload)
      id <- repository.addOne(validPayload.copy(_id = newId))
      httpResponse <- Future.successful { Ok(id) }
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  def deleteApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    (for {
      validId <- parseId(id)
      maybeApp <- repository.deleteOne(validId)
      httpResponse <-  Future.successful { maybeApp.map(app => Ok(toJson(app))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  def updateApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    (for {
      (validId, validPayload) <- parseId(id) zip parsePayload[MobileApp](req)
      maybeApp <- repository.updateOne(validId, validPayload)
      httpResponse <-  Future.successful { maybeApp.map(app => Ok(toJson(app))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  private def verifyAppAlreadyRegistered(app: MobileApp) = {
    for {
      maybeApp <- repository.getByKey(app.name, app.company, app.store)
      error <- Future {
        maybeApp.map(_ => throw new Exception(
          "Found existing app with same name, company, store combination " +
            s"(${app.name}, ${app.company}, ${app.store})"))
      }
    } yield (error)
  }
}
