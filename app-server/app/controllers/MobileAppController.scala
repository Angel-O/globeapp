package controllers

import javax.inject.Inject
import play.api.Logger
import repos.MobileAppRepository
import play.api.libs.json.Json
import apimodels.mobileapp.MobileApp
import upickle.default._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONObjectID

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
    repository.getApp(id).map({
      case Some(mobileApp) => Ok(write(mobileApp))
      case None            => NotFound
    })
  }

  def postApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating mobile app")
    val payload: String = Json.stringify(req.body)
    val app = read[MobileApp](payload)
    repository.addApp(app).map(_ => Created)
  }

  def deleteApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    repository
      .deleteApp(id)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp))
        case None            => NotFound
      })
  }

  def updateApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    val updated = req.body.validate[MobileApp].get
    repository
      .updateApp(id, updated)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp))
        case None            => NotFound
      })
  }
}
