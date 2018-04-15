package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.user.UserProfile
import apimodels.mobile.MobileApp
import apimodels.mobile.MobileApp._
import apimodels.mobile.Genre
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.toJson
import utils.ApiClient._
import utils.Bson._
import utils.FutureImplicits._
import utils.Json._
import exceptions.ServerException._
import play.api.libs.ws._

// relatedapps by appid ===> look up genre, keywords (store suggestion for later, track by user)
// intersting apps ===> lookup user info (where did u hear about us...) (APP + USER)
// new apps ==> TODO add new tag or createdDate field to apps 
// most discussed apps ==> apps appearing in many polls (APP + POLL)
// most trending apps ==> apps favorite by many users (APP + USER)

// POST endpoint ===> allow user to appreciate suggestion....
// create suggestion categories....

//TODO store info on db ===> track if user creates a poll or leaves a review based on suggestion

class AppSuggestionController @Inject()(scc: SecuredControllerComponents)(implicit ws: WSClient)
    extends SecuredController(scc) {
  
  import Config._
  
  def getRelatedApps(appId: String) = AuthenticatedAction.async {
    Logger.info(s"Retrieving apps related to app with id = $appId")
    (for {
      validId <- parseId(appId)
      request <- Get(s"$APPS_API_ROOT/apps")
      mobileApps <- parseResponseAll[MobileApp](request)
      relatedApps <- findRelatedApps(mobileApps, appId)
    } yield ( Ok(toJson(relatedApps)) ))
    .logFailure.handleRecover
  }
  
  //TODO FIX 404 when getting profile....crazy...!
  def getInterestingApps = AuthenticatedAction.async { req =>
    val userId = req.user._id.get
    Logger.info(s"Retrieving apps of interest to user with id = ${userId}")
    (for {
      (userProfileRequest, mobileAppsRequest) <- 
        Get(s"$PROFILES_API_ROOT/userprofiles/$userId") zip Get(s"$APPS_API_ROOT/apps")
      (userProfile, mobileApps) <-
        parseResponse[UserProfile](userProfileRequest) zip parseResponseAll[MobileApp](mobileAppsRequest)
      appsByGenres <- 
        findAppsByGenres(mobileApps, userProfile.favoriteCategories)
    } yield (Ok(toJson(appsByGenres))))
      .logFailure.handleRecover
  }
  
  // TODO (see main comments)
  def getNewApps = AuthenticatedAction.async { req =>
    Logger.info(s"Retrieving new apps")
    (for {
     httpResponse <- Future { Ok }
    } yield (httpResponse)).logFailure.handleRecover
  }

  private def findRelatedApps(mobileApps: Seq[MobileApp], appId: String) = {
    val keywords = mobileApps
      .find(_._id == Some(appId))
      .map(_.keywords)
      .getOrElse(Seq.empty)

    Future {
      (for {
        keyword <- keywords
        relatedApp <- mobileApps.filter(app => app.keywords.contains(keyword) && app._id != Some(appId))
      } yield (relatedApp)).distinct
    }
  }
  
  private def findAppsByGenres(mobileApps: Seq[MobileApp], genres: Seq[Genre]) = {
    Future {
      (for {
        genre <- genres
        apps <- mobileApps.filter(app => app.genre == genre)
      } yield(apps)).distinct
    }
  }
}

object Config {
  //TO do move this to config file
  val APPS_API_ROOT = "http://localhost:3001/api"
  val PROFILES_API_ROOT = "http://localhost:3005/api" 
}