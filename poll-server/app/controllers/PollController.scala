package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.poll.{Poll, Closed}, Poll._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.toJson
import repos.PollRepository
import utils.Bson._
import utils.FutureImplicits._
import utils.Json._

class PollController @Inject()(scc: SecuredControllerComponents,
                               repository: PollRepository)
    extends SecuredController(scc) {

  def getAll = AuthenticatedAction.async {
    Logger.info("Fetching polls")
    repository.getAll.map(all => Ok(toJson(all)))
  }

  def getPoll(id: String) = AuthenticatedAction.async {
    Logger.info("Fetching poll")
    (for {
      validId <- parseId(id)
      maybePoll <- repository.getById(validId)
      httpResponse <- Future.successful { maybePoll.map(poll => Ok(toJson(poll))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.recover({ case ex => BadRequest })
  }
  
  def postPoll = AuthenticatedAction.async(parse.json) { req =>
    Logger.info(s"Creating poll (userId = ${req.user._id})")
    (for {
      validPayload <- parsePayload(req)
      id <- repository.addOne(validPayload.copy(_id = newId))
      httpResponse <- Future.successful { Ok(id) }
    } yield (httpResponse)).logFailure.recover({ case ex => BadRequest })
  }

  // TODO only admin should be allowed to delete
  def deletePoll(id: String) = AuthenticatedAction.async { req =>
    Logger.info(s"Deleting poll (id = $id)")
    (for {
      validId <- parseId(id)
      maybePoll <- repository.deleteOne(id)
      httpResponse <- Future.successful { maybePoll.map(poll => Ok(toJson(poll))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.recover({ case ex => BadRequest })
  }

  // admin endpoint
  def updatePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info(s"Updating poll (id = $id)")
    (for {
      (validId, validPayload) <- parseId(id) zip parsePayload(req)
      maybePoll <- repository.updateOne(validId, validPayload)
      httpResponse <- Future.successful { maybePoll.map(poll => Ok(toJson(poll))).getOrElse(NotFound) }
    } yield (httpResponse)).logFailure.recover({ case ex => BadRequest })
  }
  
  def vote(pollId: String, optionId: Int) = AuthenticatedAction.async { req =>
    Logger.info(s"Voting (poll(id = $pollId), option(id = $optionId), user(id = ${req.user._id.get}))") 
    (for {
      validId <- parseId(pollId)
      maybePoll <- repository.getById(validId) 
      _ <- pollIsStillOpenCheck(maybePoll) 
      _ <- userHasAlreadyVotedCheck(maybePoll, req.user._id.get)
      maybeVote <- castVote(maybePoll, req.user._id.get, optionId)
      httpResponse <- persistVoteResponse(maybeVote, validId) //TODO learn scalaZ to compose options and futures nicely
    } yield (httpResponse)).logFailure.recover({ case ex => BadRequest })
  }

  private def castVote(maybePoll: Option[Poll], userId: String, optionId: Int) = 
      Future{ maybePoll.flatMap(poll => addVote(userId, optionId, poll)) }

  private def persistVoteResponse(maybePoll: Option[Poll], pollId: String) = {
    maybePoll
      .map(poll =>
        repository
          .updateOne(pollId, poll)
          .map(updated => Ok(toJson(updated)))) //Note: updated is an option
      .getOrElse(Future.successful { NotFound })
  }

  private def userHasAlreadyVotedCheck(maybePoll: Option[Poll], userId: String) = {
    // Note how the parameter is needed. Without it the exception would be thrown regardless
    // (https://stackoverflow.com/questions/30237608/throwing-exception-in-foreach-map-block)
    Future {
      maybePoll
        .flatMap(_.options.find(_.votedBy.contains(userId)))
        .map(_ => throw new Exception(
          s"user(id = $userId) has already voted for " +
            s"poll with id = ${maybePoll.get._id}")) //calling get on the option is safe at this point
    }
  }

  private def pollIsStillOpenCheck(maybePoll: Option[Poll]) = {
    Future {
      maybePoll
        .map(_.status)
        .collect({
          case Closed => throw new Exception(
            s"poll (id = ${maybePoll.get._id}) " + //calling get on the option is safe at this point
              "no longer accepting votes")
        })
    }
  }
}