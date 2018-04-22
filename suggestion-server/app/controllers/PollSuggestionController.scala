package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.common.localDateOrdering
import apimodels.mobile.MobileApp
import apimodels.mobile.MobileApp._
import apimodels.poll.Poll
import apimodels.user.UserProfile
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSClient
import services.AppDiscovery._
import utils.ApiClient._
import utils.FutureImplicits._
import utils.Json._
import config.AppConfig

// relatedapps by appid ===> look up genre, keywords (store suggestion for later, track by user)
// intersting apps ===> lookup user info (where did u hear about us...) (APP + USER)
// new apps ==> TODO add new tag or createdDate field to apps 
// most discussed apps ==> apps appearing in many polls (APP + POLL)
// most trending apps ==> apps favorite by many users (APP + USER)

// POST endpoint ===> allow user to appreciate suggestion....
// create suggestion categories....

//TODO store info on db ===> track if user creates a poll or leaves a review based on suggestion

class PollSuggestionController @Inject()(scc: SecuredControllerComponents)(implicit ws: WSClient, appConfig: AppConfig)
    extends SecuredController(scc) {
  
  import appConfig.Api._
  
  // TODO rename to about to close polls...
  def getRecentPolls = AuthenticatedAction.async { implicit req =>
    Logger.info(s"Retrieving most recent polls")
    (for {
      jsonResponse <- Get(s"$APPS_API_ROOT/apps")
      polls <- parseResponseAll[Poll](jsonResponse)
      mostRecentPolls <- Future { polls sortBy (_.closingDate) }
    } yield ( Ok(toJson(mostRecentPolls)) ))
    .logFailure.handleRecover
  }
  
  // TODO rename to polls of interest (...favorite polls comis soon...)
  def getInterestingPolls = AuthenticatedAction.async { implicit req =>
    val userId = req.user._id.get
    Logger.info(s"Retrieving polls of interest to user with id = ${userId}")
    (for {
      ((userProfileJsonResponse, mobileAppsJsonResponse), pollsJsonResponse) <- 
        Get(s"$PROFILES_API_ROOT/userprofiles/$userId") zip 
        Get(s"$APPS_API_ROOT/apps") zip 
        Get(s"$POLLS_API_ROOT/polls")
      ((userProfile, mobileApps), polls) <-
        parseResponse[UserProfile](userProfileJsonResponse) zip 
        parseResponseAll[MobileApp](mobileAppsJsonResponse) zip 
        parseResponseAll[Poll](pollsJsonResponse) 
      interestingAppsIds <- 
        findAppsByGenres(mobileApps, userProfile.favoriteCategories) map { apps => apps.flatMap(_._id) }
      interestingPolls <- Future { polls.filter(poll => interestingAppsIds.contains(poll.mobileAppId)) }
    } yield ( Ok(toJson(interestingPolls)) ))
    .logFailure.handleRecover
  }
}