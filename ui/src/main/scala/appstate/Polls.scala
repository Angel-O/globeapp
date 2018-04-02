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

import apimodels.poll.{Poll, PollOption, Open}
import java.time.LocalDate

// Model
case class Polls(polls: Seq[Poll])
case object Polls {
  def apply() = new Polls(Seq.empty)
}

// Primary actions
case object FetchPolls extends Action
case class FetchPoll(pollId: String) extends Action
case class CreatePoll(title: String,
                      content: String,
                      mobileAppId: String,
                      createdBy: String,
                      closingDate: LocalDate,
                      options: Seq[String])
    extends Action

// Secondary actions
case class PollsFetched(polls: Seq[Poll]) extends Action

// Action handler
class PollHandler[M](modelRW: ModelRW[M, Seq[Poll]])
    extends ActionHandler(modelRW)
    with PollEffects {
  override def handle = {
    case FetchPolls          => effectOnly(fetchPollsEffect())
    case PollsFetched(polls) => updated(polls)
    case CreatePoll(title,
                    content,
                    mobileAppId,
                    createdBy,
                    closingDate,
                    options) =>
      effectOnly(
        createPollEffect(title,
                         content,
                         mobileAppId,
                         createdBy,
                         closingDate,
                         options))
  }
}

// Effects
trait PollEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.jwt._, utils.persist._, utils._
  import diode.{Effect, NoAction}
  import config._

  def fetchPollsEffect() =
    Effect(
      Get(url = s"$POLL_SERVER_ROOT/api/polls")
        .map(xhr => PollsFetched(read[Seq[Poll]](xhr.responseText)))
        .redirectToLoginOnFailure)

  def createPollEffect(title: String,
                       content: String,
                       mobileAppId: String,
                       createdBy: String,
                       closingDate: LocalDate,
                       options: Seq[String]) = {
    val poll = Poll(
      _id = None, title, //TODO fix this...
      content,
      mobileAppId,
      createdBy = Some(createdBy),
      closingDate,
      status = Open,
      options.map(PollOption.apply))
      
    Effect(
      Post(url = s"$POLL_SERVER_ROOT/api/polls", payload = write(poll))
        .map(_ => NoAction)
        .redirectToLoginOnFailure)
  }
}

// Selector
object PollSelector extends ReadConnect[AppModel, Seq[Poll]] {
  def getPolls() = model
  def getPollById(id: String) = getPolls.find(_._id == Some(id))
  def getPollsPartecipated(userId: String) =
    getPolls.filter(_.options.exists(_.votedBy.contains(userId)))

  val cursor = AppCircuit.pollSelector
  val circuit = AppCircuit
  //def onPollUpdate(connector: => Unit) = circuit.subscribe(cursor)(_ => connector)
}

trait PollSelector extends GenericConnect[AppModel, Seq[Poll]] {
  def getPolls() = model
  def getPollById(id: String) = getPolls.find(_._id == Some(id))
  def getPollsPartecipated(userId: String) =
    getPolls.filter(_.options.exists(_.votedBy.contains(userId)))

  val cursor = AppCircuit.pollSelector
  val circuit = AppCircuit
  connect()
}
