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

  def getUserProfile = AuthenticatedAction.async { req => 
    Logger.info("Fetching user profile")
    (for {
      maybeProfile <- repository.getByUser(req.user._id.get)
      httpResponse <- Future.successful { maybeProfile.map(profile => Ok(toJson(profile))).getOrElse(NotFound) }
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
}