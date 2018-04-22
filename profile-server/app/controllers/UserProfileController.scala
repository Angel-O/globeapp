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

class UserProfileController @Inject()(scc: SecuredControllerComponents,
                               repository: UserProfileRepository)
    extends SecuredController(scc) {

  def getUserProfile(userId: String) = Action.async { 
    Logger.info("Fetching user profile")
    (for {
      userProfile <- 
        repository.getByUser(userId) map 
        { _.getOrElse(throw NotFoundException("Profile not found")) }
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
    Logger.info(s"Saving favorite app (id = $appId)")
    (for {
      validId <- parseId(appId)
      userProfile <- 
        repository.getByUser(req.user._id.get) map 
        { _.getOrElse(throw new NotFoundException("Profile not found")) }
      updated <- repository.updateOne(userProfile._id.get, userProfile.addFavoriteApp(appId))
      httpResponse <- Future.successful { Ok(toJson(updated)) }
    } yield(httpResponse))
    .logFailure.handleRecover
  }
  
  def removeAppFromFavorites(appId: String) = AuthenticatedAction.async { req =>
    Logger.info(s"Removing favorite app (id = $appId)")
    (for {
      validId <- parseId(appId)
      userProfile <- 
        repository.getByUser(req.user._id.get) map 
        { _.getOrElse(throw new NotFoundException("Profile not found")) }
      updated <- repository.updateOne(userProfile._id.get, userProfile.removeFromFavoriteApps(appId))
      httpResponse <- Future.successful { Ok(toJson(updated)) }
    } yield(httpResponse))
    .logFailure.handleRecover
  }
  
  //TODO finish this afer moving endpoint to common config file...
  def getFavoriteApps = AuthenticatedAction.async { req => 
    val userId = req.user._id.get
    Logger.info(s"Fetching favorite apps (user id = $userId)")
    (for {
      userProfile <- 
        repository.getByUser(userId) map 
        { _.getOrElse(throw new NotFoundException("Profile not found")) }
      
      apps <- Future{} // Get()
      
    } yield (???))
  }
  
  //TODO allow to update user profile (to save favorite apps, change favorite categories...)
  //TODO endpoint to get favorite apps
}