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

import apimodels.poll.{Poll, PollOption}
import java.time.LocalDate

// Model
case class Polls(polls: Seq[Poll])
case object Polls{
  def apply() = new Polls(Seq.empty)
}

// Primary actions
case object FetchPolls extends Action
case class FetchPoll(pollId: String) extends Action
case class CreatePoll(
            title: String,
            content: String,
            mobileAppId: String,
            createdBy: String,
            closingDate: LocalDate,
            status: String,
            options: Seq[PollOption]) extends Action

// Secondary actions
case class PollsFetched(polls: Seq[Poll]) extends Action


// Action handler
class PollHandler[M](modelRW: ModelRW[M, Seq[Poll]]) extends ActionHandler(modelRW) with PollEffects{
  override def handle = {
    case FetchPolls => effectOnly(fetchPollsEffect())
    case PollsFetched(polls) => updated(polls)
    case CreatePoll(title, content, mobileAppId, createdBy, closingDate, status, options) => ???
  }
}
  
 // Effects
trait PollEffects extends Push{
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import upickle.default._
  import utils.api._, utils.jwt._, utils.persist._
  import diode.{Effect, NoAction}
  import config._
  
  //TODO implement real api calls
  import mock.PollApi._

  def fetchPollsEffect() =
    Effect(Future { 1 }.map(_ => PollsFetched(getAll)))
}

// Selector
trait PollSelector extends GenericConnect[AppModel, Seq[Poll]] {
  
  def getPolls() = model
  def getPollById(id: String) = getPolls.find(_.id == id)
  def getPollsPartecipated(userId: String) = getPolls.filter(_.options.exists(_.votedBy.contains(userId)))

  val cursor = AppCircuit.pollSelector
  val circuit = AppCircuit
  connect()
}
