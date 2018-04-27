package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.Push
import config._
import navigation.URIs._

import diode.data.Pot
import diode.data.PotState._
import diode.data.{Ready, Pending}

import apimodels.user.UserProfile
import apimodels.mobile.{MobileApp, Genre}
import java.time.LocalDate

// Model
case class UiUserData(
  favoriteCategories: Seq[Genre] = Seq.empty, 
  favoriteAppsIds: Seq[String] = Seq.empty, 
  persistentState: Option[PersistentState] = None)
case object UiUserData{
  def apply() = new UiUserData(persistentState = PersistentState())
}
case class UiUser(state: UiUserData = UiUserData.apply)
case object UiUser {
  def apply() = new UiUser()
}

// Primary actions
case object FetchUserProfile extends Action
case class AddAppToFavorites(appId: String) extends Action
case class RemoveAppFromFavorites(appId: String) extends Action

// Secondary actions
case class UserProfileFetched(favoriteCategories: Seq[Genre], favoriteAppsIds: Seq[String]) extends Action
case class AppAddedToFavorites(appId: String) extends Action
case class AppRemovedFromFavorites(appId: String) extends Action

// Action handler
class UiUserHandler[M](modelRW: ModelRW[M, UiUserData])
  extends ActionHandler(modelRW)
  with UiUserEffects with AuthEffects {
  override def handle = {
    case FetchUserProfile =>
      effectOnly(fetchUserProfileEffect())
    case AddAppToFavorites(appId) =>
      updated(
        value.copy(favoriteAppsIds = value.favoriteAppsIds :+ appId),
        addToFavoritesEffect(appId))
    case RemoveAppFromFavorites(appId) =>
      updated(
        value.copy(favoriteAppsIds = value.favoriteAppsIds.filterNot(_ == appId)),
        removeFromFavoritesEffect(appId))
    case UserProfileFetched(categories, appIds) =>
      updated(value.copy(favoriteCategories = categories, favoriteAppsIds = appIds),
      PersistentState() match {
        case Some(state) => persistStorageEffect(state.copy(favoriteAppsIds = appIds))
        case None => persistStorageEffect(new PersistentState(favoriteAppsIds = appIds))
      }) 

    // External actions
    case UserLoggedIn(_, user) => effectOnly(fetchUserProfileEffect(user._id))
  }
}

// Effects
trait UiUserEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.redirect._, utils.json._, utils.redirect._
  import diode.{Effect, NoAction}
  import config._

  import play.api.libs.json.Json._
  
  import AuthSelector.getUserId
  
  def fetchUserProfileEffect(userId: Option[String] = None) = {
    val id = userId match { case Some(x) => x case None => getUserId }
    Effect( 
        Get(s"$USERPROFILE_SERVER_ROOT/api/userprofiles/$id") map 
        { xhr => { 
          val profile = read[UserProfile](xhr.responseText) 
          UserProfileFetched(
              favoriteCategories = profile.favoriteCategories, 
              favoriteAppsIds = profile.favoriteApps) } 
        } )
  }
  
  def addToFavoritesEffect(appId: String) = {
    Effect(
        (Put(
          url = s"$USERPROFILE_SERVER_ROOT/api/userprofiles/$getUserId/addtofavorites", 
          payload = appId, contentHeader = TEXT_CONTENT_HEADER) map 
        { _ => NoAction }).redirectOnFailure) //TODO fix redirect...needs to throw 401
  }
  
  def removeFromFavoritesEffect(appId: String) = {
    Effect(
        (Put(url = s"$USERPROFILE_SERVER_ROOT/api/userprofiles/$getUserId/removefromfavorites", 
        payload = appId, contentHeader = TEXT_CONTENT_HEADER) map 
        { _ => NoAction }).redirectOnFailure) //TODO fix redirect...needs to throw 401
  }

  def noEffect = Effect(Future successful { NoAction })
}

// Selector
object UiUserSelector extends AppModelSelector[UiUserData] {
  def getFavoriteAppsIds = model.favoriteAppsIds
  def getFavoriteCategories = model.favoriteCategories
  def getAppIsFavorite(appId: String) = model.favoriteAppsIds contains appId

  val cursor = AppCircuit.uiUserSelector
}
