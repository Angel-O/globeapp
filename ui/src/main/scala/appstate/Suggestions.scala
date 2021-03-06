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

case class SuggestionsState(relatedApps: Seq[MobileApp], interestingApps: Seq[MobileApp], mostDebatedApps: Seq[MobileApp]) // TODO add more
case object SuggestionsState {
  def apply() = new SuggestionsState(relatedApps = Seq.empty, interestingApps = Seq.empty, mostDebatedApps = Seq.empty)
}
case class Suggestions(state: SuggestionsState)
case object Suggestions {
  def apply() = new Suggestions(SuggestionsState())
}

// Primary Actions
case class FetchRelatedApps(appId: String) extends Action
case object FetchInterestingApps extends Action
case class FetchMostDeabatedApps(amount: Int) extends Action

// Derived Actions
case class RelatedAppsFetched(apps: Seq[MobileApp]) extends Action
case class InterstingAppsFetched(apps: Seq[MobileApp]) extends Action
case class MostDebatedAppsFetched(apps: Seq[MobileApp]) extends Action

// Action handler
class SuggestionHandler[M](modelRW: ModelRW[M, SuggestionsState])
    extends ActionHandler(modelRW)
    with SuggestionsEffects {
  override def handle = {
    case FetchRelatedApps(appId)  => effectOnly(fetchRelatedAppsEffect(appId))
    case RelatedAppsFetched(apps) => updated(value.copy(relatedApps = apps))
    case FetchInterestingApps => effectOnly(fetchInterestingAppsEffect())
    case InterstingAppsFetched(apps) => updated(value.copy(interestingApps = apps))
    case FetchMostDeabatedApps(amount) => effectOnly(fetchMostDebatedAppsEffect(amount))
    case MostDebatedAppsFetched(apps) => updated(value.copy(mostDebatedApps = apps))
  }
}

// Effects
trait SuggestionsEffects extends Push {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.jwt._, utils.persist._, utils.json._
  import diode.{Effect, NoAction}
  import config._

  import play.api.libs.json.Json._
  import play.api.libs.json._

  def fetchRelatedAppsEffect(id: String) = {
    Effect(
      Get(url = s"$SUGGESTIONS_SERVER_ROOT/api/relatedapps/$id")
        .map(xhr => RelatedAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
  }
  
  def fetchInterestingAppsEffect() = {
    Effect(
      Get(url = s"$SUGGESTIONS_SERVER_ROOT/api/interestingapps")
        .map(xhr => InterstingAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
  }
  
  def fetchMostDebatedAppsEffect(amount: Int) = {
    Effect(
      Get(url = s"$SUGGESTIONS_SERVER_ROOT/api/mostdebatedapps?amount=$amount")
        .map(xhr => MostDebatedAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
  }
}

// Selector
object SuggestionsSelector extends AppModelSelector[SuggestionsState] {
  import scala.util.Random
  
  def getSuggestedMobileApps(maxAmount: Int) =
    Random shuffle model.relatedApps take (maxAmount) sortBy (_.store) 
    
  def getInterestingMobileApps(maxAmount: Int) = 
    Random shuffle model.interestingApps take (maxAmount) sortBy (_.store)
    
  def getMostDebatedMobileApps() = 
    model.mostDebatedApps

  val cursor = circuit.suggestionSelector //ModelRW[M, T]
}
