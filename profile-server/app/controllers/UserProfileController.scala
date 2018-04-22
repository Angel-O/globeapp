package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.user.UserProfile, UserProfile._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.toJson
import repos.UserProfileRepository
import utils.Bson._
import utils.FutureImplicits._
import utils.Json._
import utils.ApiClient._
import exceptions.ServerException._
import config.AppConfig
import play.api.libs.ws.WSClient
import apimodels.mobile.MobileApp

class UserProfileController @Inject()(scc: SecuredControllerComponents,
                               repository: UserProfileRepository)(implicit appConfig: AppConfig, ws: WSClient)
    extends SecuredController(scc) {
  
  import appConfig.Api._

  def getUserProfile(userId: String) = Action.async { 
    Logger.info(s"Fetching user profile (userId = $userId)")
    (for {
      validId <- parseId(userId) // not neccessary, but it provides a useful error msg
      userProfile <- fetchUserProfile(validId)
      httpResponse <- Future.successful { Ok(toJson(userProfile)) }
    } yield (httpResponse))
    .logFailure.handleRecover
  }
  
  def postUserProfile = Action.async(parse.json) { req =>
    Logger.info(s"Creating user profile...")
    (for {
      validPayload <- parsePayload(req) 
      id <- 
        repository.addOne(validPayload.copy(_id = newId)) andThen 
        { case _ => Logger.info(s"...User profile created (userId = ${validPayload.userId})") }
      httpResponse <- Future.successful { Ok(id) }
    } yield (httpResponse))
    .logFailure.handleRecover
  }
  
  def addAppToFavorites(appId: String) = AuthenticatedAction.async { req =>
    val userId = req.user._id.get
    Logger.info(s"Saving favorite app (id = $appId)")
    (for {
      validId <- parseId(appId)
      userProfile <- fetchUserProfile(userId)
      updated <- repository.updateOne(userProfile._id.get, userProfile.addFavoriteApp(appId))
      httpResponse <- Future.successful { Ok(toJson(updated)) }
    } yield (httpResponse))
    .logFailure.handleRecover
  }
  
  def removeAppFromFavorites(appId: String) = AuthenticatedAction.async { req =>
    val userId = req.user._id.get
    Logger.info(s"Removing favorite app (id = $appId)")
    (for {
      validId <- parseId(appId)
      userProfile <- fetchUserProfile(userId)
      updated <- repository.updateOne(userProfile._id.get, userProfile.removeFromFavoriteApps(appId))
      httpResponse <- Future.successful { Ok(toJson(updated)) }
    } yield (httpResponse))
    .logFailure.handleRecover
  }
  
  def getFavoriteApps = AuthenticatedAction.async { implicit req => 
    val userId = req.user._id.get
    Logger.info(s"Fetching favorite apps (user id = $userId)")
    (for {
      (userProfile, jsonResponse) <- 
        fetchUserProfile(userId) zip
        Get(s"$APPS_API_ROOT/apps")
      apps <- parseResponseAll[MobileApp](jsonResponse)
      // favoriteApps <- Future { apps.filter(app => app._id.map(userProfile.favoriteApps.contains).getOrElse(false)) } //INEFFICIENT 
      favoriteApps <- Future { userProfile.favoriteApps.flatMap(appId => apps.find(_._id == Some(appId))) }
    } yield ( Ok(toJson(favoriteApps)) ))
    .logFailure.handleRecover
  }
  
  private def fetchUserProfile(userId: String) = {
    repository.getByUser(userId) map 
    { _.getOrElse(throw new NotFoundException("Profile not found")) }
  }
  
  //TODO allow to update user profile (to save favorite apps, change favorite categories...)
}