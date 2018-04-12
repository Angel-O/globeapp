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

case class SuggestionsState(relatedApps: Seq[MobileApp]) // TODO add more
case object SuggestionsState {
  def apply() = new SuggestionsState(relatedApps = Seq.empty)
}
case class Suggestions(state: SuggestionsState)
case object Suggestions {
  def apply() = new Suggestions(SuggestionsState())
}

// Primary Actions
case class FetchRelatedApps(appId: String) extends Action

// Derived Actions
case class RelatedAppsFetched(apps: Seq[MobileApp]) extends Action

// Action handler
class SuggestionHandler[M](modelRW: ModelRW[M, SuggestionsState])
    extends ActionHandler(modelRW)
    with SuggestionsEffects {
  override def handle = {
    case FetchRelatedApps(appId)  => effectOnly(fetchRelatedAppsEffect(appId))
    case RelatedAppsFetched(apps) => updated(value.copy(relatedApps = apps))
  }
}

// Effects
trait SuggestionsEffects extends Push {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.jwt._, utils.persist._
  import diode.{Effect, NoAction}
  import config._

  import play.api.libs.json.Json._
  import play.api.libs.json._

  def fetchRelatedAppsEffect(id: String) = {
    Effect(
      Get(url = s"$SUGGESTIONS_SERVER_ROOT/api/relatedapps/$id")
        .map(xhr => RelatedAppsFetched(read[Seq[MobileApp]](xhr.responseText))))
  }
}

object SuggestionsSelector extends ReadConnect[AppModel, SuggestionsState] {
  def getSuggestedMobileApps() =
    model.relatedApps take (5) sortBy (_.name) // only 5...TODO scramble them...

  val cursor = AppCircuit.suggestionSelector
  val circuit = AppCircuit
}
