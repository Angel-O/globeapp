package controllers

import javax.inject.Inject
import play.api.Logger
import repos.MobileAppRepository
import play.api.libs.json.Json
import apimodels.{MobileApp => ApiApp}
import upickle.default._
import scala.concurrent.ExecutionContext.Implicits.global
import models.MobileApp, models.Helpers._
import reactivemongo.bson.BSONObjectID 

class MobileAppController @Inject()(scc: SecuredControllerComponents,
                                    repository: MobileAppRepository)
    extends SecuredController(scc) {

  def getAll = AuthenticatedAction.async {
    Logger.info("Fetching mobile apps")
    repository.getAll.map(all => Ok(write(all.map(_.toApi))))
  }
  
  def getApp(id: String) = AuthenticatedAction.async {
    Logger.info("Fetching mobile app")
    
//    BSONObjectID.parse(id).map(validId => ...)
//    .recover({case ex => BadRequest})
    
    repository.getApp(BSONObjectID.parse(id).get).map({
      case Some(mobileApp) => Ok(write(mobileApp.toApi))
      case None => NotFound
    })
  }

  def postApp = Action.async(parse.json) { req =>
    val payload: String = Json.stringify(req.body)
    val apiApp = read[ApiApp](payload)
    repository.addApp(apiApp.toModel).map(_ => Created)
  }

  def deleteApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    val id: String = req.body.as[String]
    repository
      .deleteApp(BSONObjectID.parse(id).get)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp.toApi))
        case None            => NotFound
      })
  }

  def updateApp = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    //val payload: String = Json.stringify(req.body)
    val id = req.user.id
    val updated = req.body.validate[MobileApp].get //read[User](payload)
    repository
      .updateApp(BSONObjectID.parse(id).get, updated)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp.toApi))
        case None            => NotFound
      })
  }
}
