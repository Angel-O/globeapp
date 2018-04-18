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
import apimodels.poll.Poll
import apimodels.common._
import play.api.mvc.Request
import play.api.libs.json.JsValue
import play.api.mvc.AnyContent

// relatedapps by appid ===> look up genre, keywords (store suggestion for later, track by user)
// intersting apps ===> lookup user info (where did u hear about us...) (APP + USER)
// new apps ==> TODO add new tag or createdDate field to apps 
// most discussed apps ==> apps appearing in many polls (APP + POLL)
// most trending apps ==> apps favorite by many users (APP + USER)

// POST endpoint ===> allow user to appreciate suggestion....
// create suggestion categories....

//TODO store info on db ===> track if user creates a poll or leaves a review based on suggestion

class PollSuggestionController @Inject()(scc: SecuredControllerComponents)(implicit ws: WSClient)
    extends SecuredController(scc) {
  
  import Config._
  
  def getRecentPolls = AuthenticatedAction.async { implicit req =>
    Logger.info(s"Retrieving most recent polls")
    (for {
      jsonResponse <- Get(s"$APPS_API_ROOT/apps")
      polls <- parseResponseAll[Poll](jsonResponse)
      mostRecentPolls <- Future { polls sortBy (_.closingDate) }
    } yield ( Ok(toJson(mostRecentPolls)) ))
    .logFailure.handleRecover
  }
}