package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.mobile.MobileApp, MobileApp._
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
  
  def getRelatedApps(appId: String) = AuthenticatedAction.async {
    Logger.info(s"Retrieving apps related to app with id $appId")
    (for {
      validId <- parseId(appId)
      request <- Get(s"http://localhost:3001/api/apps")
      mobileApps <- parseResponseAll(request)
      relatedApps <- findRelatedApps(mobileApps, appId)
    } yield ( Ok(toJson(relatedApps)) ))
    .logFailure.handleRecover
  }
  
  // TODO create user-interest service...storing user interest on sign up
  def getInterestingApps = AuthenticatedAction.async { req =>
    Logger.info(s"Retrieving apps of interest to user with id ${req.user._id}")
    (for {
     httpResponse <- Future { Ok }
    } yield (httpResponse)).logFailure.handleRecover
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
}