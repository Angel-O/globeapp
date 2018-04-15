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
import exceptions.ServerException._

class UserProfileController @Inject()(scc: SecuredControllerComponents,
                               repository: UserProfileRepository)
    extends SecuredController(scc) {

  def getUserProfile(userId: String) = Action.async { 
    Logger.info("Fetching user profile")
    (for {
      maybeProfile <- repository.getByUser(userId)
      httpResponse <- Future { maybeProfile.map(profile => Ok(toJson(profile))).getOrElse(throw NotFoundException("Profile not found")) }
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  def postUserProfile = Action.async(parse.json) { req =>
    Logger.info(s"Creating user profile...")
    (for {
      validPayload <- parsePayload(req) 
      id <- {
        repository.addOne(validPayload.copy(_id = newId)) 
        .andThen { case _ => Logger.info(s"...User profile created (userId = ${validPayload.userId})") }
      }
      httpResponse <- Future.successful { Ok(id) }
    } yield (httpResponse)).logFailure.handleRecover
  }
  
  //TESTING
  def getAll = AuthenticatedAction.async { req => 
    Logger.info("Fetching user profile")
    repository.getAll.map(profiles => Ok(toJson(profiles))).logFailure.handleRecover
  }
  
  //TODO allow to update user profile (to save favorite apps, change favorite categories...)
  //TODO endpoint to get favorite apps
}