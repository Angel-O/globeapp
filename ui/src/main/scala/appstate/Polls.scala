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
case class CastVote(pollId: String, optionId: Int) extends Action

// Secondary actions
case class PollsFetched(polls: Seq[Poll]) extends Action
case class VoteCasted(poll: Poll) extends Action

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
    case CastVote(pollId, optionId) => effectOnly(castVoteEffect(pollId, optionId)) 
    case VoteCasted(poll) => {
      value
        .zipWithIndex
        .find({ case (p, _) => p._id == poll._id })
        .map({ case (_, i) => updated(value.updated(i, poll)) })
        .getOrElse(noChange)
    }
  }
}

// Effects
trait PollEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.jwt._, utils.persist._, utils.redirect._, utils.json._
  import diode.{Effect, NoAction}
  import config._

  def fetchPollsEffect() =
    Effect(
      Get(url = s"$POLL_SERVER_ROOT/api/polls")
        .map(xhr => PollsFetched(read[Seq[Poll]](xhr.responseText)))
        .redirectOnFailure)

  def createPollEffect(title: String,
                       content: String,
                       mobileAppId: String,
                       createdBy: String,
                       closingDate: LocalDate,
                       options: Seq[String]) = {
    val poll = Poll(
      title,
      content,
      mobileAppId,
      createdBy,
      closingDate,
      status = Open,
      options.map(PollOption.apply))
      
    Effect(
      Post(url = s"$POLL_SERVER_ROOT/api/polls", payload = write(poll))
        .map(_ => NoAction)
        .redirectOnFailure)
  }
  
  def castVoteEffect(pollId: String, optionId: Int) = {
    Effect(
      Put(url = s"$POLL_SERVER_ROOT/api/polls/$pollId/$optionId")
        .map(xhr => VoteCasted(read[Poll](xhr.responseText)))
        .redirectOnFailure)
  }
}

// Selector
object PollSelector extends AppModelSelector[Seq[Poll]] {
  def getPolls() = model.sortBy(_.closingDate)
  def getPollById(id: String) = getPolls.find(_._id == Some(id))
  def getPollsPartecipated(userId: String) =
    getPolls.filter(_.options.exists(_.votedBy.contains(userId)))
    
  val cursor = circuit.pollSelector
}
