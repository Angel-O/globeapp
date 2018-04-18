package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler
import utils.Push
import config._
import navigation.URIs._

// import diode.data.Pot
// import diode.data.PotState._
// import diode.data.{Ready, Pending}
import apimodels.mobile.MobileApp
import scala.concurrent.Promise

case class MobileApps(apps: Seq[MobileApp])
case object MobileApps {
  def apply() = new MobileApps(Seq.empty)
}

// Primary Actions
case object FetchAllMobileApps extends Action
case object FetchFavoriteMobileApps extends Action // no need for id parameter, using jwt Token for identification...
case class FetchMobileApp(mobileAppId: String) extends Action
case class AddMobileAppToFavorites(mobileAppId: String) extends Action
case class RemoveMobileAppFromFavorites(mobileAppId: String) extends Action

// Derived Actions
case class MobileAppsFetched(apps: Seq[MobileApp]) extends Action
case class MobileAppFetched(app: MobileApp) extends Action
case class MobileAppAddedToFavorites(mobileAppId: String) extends Action //TODO do I need an id here ??
case class MobileAppRemovedFromFavorites(mobileAppId: String) extends Action //TODO do I need an id here ??

// Action handler
class MobileAppsHandler[M](modelRW: ModelRW[M, Seq[MobileApp]])
    extends ActionHandler(modelRW)
    with MobileAppsEffects {
  override def handle = {
    case FetchAllMobileApps                         => effectOnly(fetchMobileAppsEffect())
    case FetchFavoriteMobileApps                    => ???
    case AddMobileAppToFavorites                    => ???
    case RemoveMobileAppFromFavorites               => ???
    case FetchMobileApp(id: String)                 => effectOnly(fetchMobileAppEffect(id))
    case MobileAppsFetched(apps)                    => updated(apps)
    case MobileAppAddedToFavorites(mobileAppId)     => ???
    case MobileAppRemovedFromFavorites(mobileAppId) => ???
    case MobileAppFetched(app)                      => updated(Seq(app)) //THIS IS NOT RIGHT
  }
}

// Effects
trait MobileAppsEffects extends Push {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.jwt._, utils.persist._, utils.json._
  import diode.{Effect, NoAction}
  import config._
  
  import play.api.libs.json.Json._
  import play.api.libs.json._
  
  
  def fetchMobileAppEffect(id: String) = {
    Effect(Get(url = s"$MOBILEAPP_SERVER_ROOT/api/apps/$id")
        .map(xhr => MobileAppFetched(read[MobileApp](xhr.responseText))))
  }
  
  def fetchMobileAppsEffect() = {
    Effect(
      Get(url = s"$MOBILEAPP_SERVER_ROOT/api/apps")
        .map(xhr => MobileAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
  }
}

// Selector
object MobileAppsSelector extends AppModelSelector[Seq[MobileApp]]{
  def getMobileApps() = model.sortBy(_.name)
  def getMobileAppById(id: String) = getMobileApps.find(_._id == Some(id)) 
  
  val cursor = AppCircuit.mobileAppSelector
  val circuit = AppCircuit
}

//  def fetchMobileAppsEffect() = {
//    Effect(
//      Get(url = s"$MOBILEAPP_SERVER_ROOT/api/apps")
//        .map(xhr => MobileAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
//  }

//  def fetchMobileAppEffect(id: String) = {
//    Effect(Get(url = s"$MOBILEAPP_SERVER_ROOT/api/apps/$id").map(xhr => MobileAppFetched(read[MobileApp](xhr.responseText))))
//  }

  //import mock.MobileAppApi._
  // def fetchMobileAppEffectTEST(id: String) = {
  //   Effect(delay(10000).map(app => MobileAppFetched(app)))
  // }
  
  // //TEST
  // def delay(milliseconds: Int): Future[MobileApp] = {
  //   import scalajs.js
  //   val p = Promise[MobileApp]()
  //   js.timers.setTimeout(milliseconds) {
  //     p.success((getById))
  //   }
  //   p.future
  // }
