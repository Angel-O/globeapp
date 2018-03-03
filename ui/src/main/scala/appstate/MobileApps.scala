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
import apimodels.MobileApp

case class MobileApps(apps: Seq[MobileApp])
case object MobileApps {
  def apply() = new MobileApps(Seq.empty)
}

// Primary Actions
case object FetchAllMobileApps extends Action
case object FetchFavoriteMobileApps extends Action // no need for id parameter, using jwt Token for identification...
case class AddMobileAppToFavorites(mobileAppId: String) extends Action
case class RemoveMobileAppFromFavorites(mobileAppId: String) extends Action

// Derived Actions
case class MobileAppsFetched(apps: Seq[MobileApp]) extends Action
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
    case MobileAppsFetched(apps)                    => updated(apps)
    case MobileAppAddedToFavorites(mobileAppId)     => ???
    case MobileAppRemovedFromFavorites(mobileAppId) => ???
  }
}

// Effects
trait MobileAppsEffects extends Push {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import upickle.default._
  import utils.api._, utils.jwt._, utils.persist._
  import diode.{Effect, NoAction}
  import config._

  //TODO implement real api calls
  import mock.MobileAppApi._

  def fetchMobileAppsEffect() =
    Effect(Future { 1 }.map(_ => MobileAppsFetched(getAll)))
}

// Selector
trait MobileAppsSelector extends GenericConnect[AppModel, Seq[MobileApp]] {

  def getAllApps() = model

  val cursor = AppCircuit.mobileAppSelector
  val circuit = AppCircuit
  connect()
}
