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
    parseId(id)
      .flatMap(validId =>
        repository
          .getById(validId)
          .map({
            case Some(poll) => Ok(toJson(poll))
            case None       => NotFound
          }))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def postPoll = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating poll")
    req.body
      .validate[Poll]
      .map(poll =>
        repository
          .addOne(poll.copy(_id = newId))
          .map(id => Created(id)))
      .getOrElse(Future { BadRequest("Invalid payload") })
  }

  // TODO only admin should be allowed to delete
  def deletePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting poll")
    parseId(id).flatMap(
      validId =>
        repository
          .deleteOne(validId)
          .map({
            case Some(poll) => Ok(toJson(poll))
            case None       => NotFound
          }))
  }

  // admin endpoint
  def updatePollOLD(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating poll")
    parseId(id).flatMap(validId => {
      val updated = req.body.validate[Poll].get
      repository
        .updateOne(validId, updated)
        .map({
          case Some(poll) => Ok(toJson(poll))
          case None       => NotFound
        })
    })
  }

  // admin endpoint
  def updatePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info(s"Updating poll (id = $id)")
    (for {
      (validId, validPayload) <- parseId(id) zip parsePayload(req) // they can run concurrently
      maybePoll <- repository.updateOne(validId, validPayload)
      httpResponse <- Future { maybePoll.fold(NotFound)(poll => Ok(toJson(poll)).asInstanceOf) }
    } yield (httpResponse)).recover({ case ex => { Logger.error(ex.getMessage); BadRequest } })
  }
  
  def vote(pollId: String, optionId: String) = AuthenticatedAction.async { req =>
    Logger.info(s"Voting (poll(id = $pollId), option(id = $optionId), user(id = ${req.user._id.get}))") 
    (for {
      validId <- parseId(pollId)
      maybePoll <- repository.getById(validId) 
      _ <- pollIsStillOpenCheck(maybePoll)
      _ <- userHasAlreadyVotedCheck(maybePoll, req.user._id.get)
      maybeVote <- castVote(maybePoll, req.user._id.get, optionId) 
      httpResponse <- persistVoteResponse(maybeVote, validId)
    } yield (httpResponse)).recover({ case ex => { Logger.error(ex.getMessage); BadRequest } }) 
  }

  // TODO verify poll is still open
  // TODO use id for option???...options should be unique...
  // Note flatten(flatMap after parsing id is not enough because options and future do not compose...)
  def voteOld(pollId: String, optionId: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Voting")
    parseId(pollId)
      .flatMap(validId =>
        repository.getById(validId)
          .map(maybePoll => maybePoll
            .map(poll => {
              addVote(req.user._id.get, optionId, poll)
                .map(poll =>
                  repository
                    .updateOne(validId, poll)
                    .map(updated => Ok(toJson(updated))))
                .getOrElse(Future { BadRequest }) //poll option not found
            })
            .getOrElse(Future { NotFound }))).flatten //poll not found. 
      .recover({ case ex => { Logger.error(ex.getMessage); BadRequest } })
  }
  
  def voteforComprehension(pollId: String, optionId: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Voting") 
    //for comprehension running futures sequentially
    (for {
      validId <- parseId(pollId)
      maybePoll <- repository.getById(validId)  
      _ <- userHasAlreadyVotedCheck(maybePoll, req.user._id.get)
      //maybeVote is an Option[Poll] (calling it vote is more idiomatic)
      maybeVote <- castVote(maybePoll, req.user._id.get, optionId) 
      httpResponse <- persistVoteResponse(maybeVote, validId)
      //invalid id, or poll option not found, that is (invalid pollOption for current poll...bad request makes sense)
      // or user has already voted...
    } yield (httpResponse)).recover({ case ex => { Logger.error(ex.getMessage); BadRequest } }) 
  }
  
  private def castVote(maybePoll: Option[Poll], userId: String, optionId: String) = 
      Future{ maybePoll.flatMap(poll => addVote(userId, optionId, poll)) }

  private def persistVoteResponse(maybePoll: Option[Poll], pollId: String) =
      maybePoll
        .map(poll =>
          repository
            .updateOne(pollId, poll) 
            // if the above fails the exception is picked up 
            // by the recover clause outside the for comprehension
            //.map(_ => throw new Exception("bla bla")) 
            .map(updated => Ok(toJson(updated)))) //Note: updated is an option
        .getOrElse(Future { NotFound }) // poll not found

  private def userHasAlreadyVotedCheck(maybePoll: Option[Poll], userId: String): Future[Option[Unit]] = {
    Future {
      maybePoll
        .map(_.options
          .find(_.votedBy.contains(userId))
          .fold(Unit)(throw new Exception(
            s"user(id = $userId) has already voted for " +
              s"poll with id = ${maybePoll.get._id}")))
    }
  }

  private def pollIsStillOpenCheck(maybePoll: Option[Poll]): Future[Unit] = {
    Future {
      maybePoll
        .find(_.status == Closed)
        .fold(Unit)(throw new Exception(s"poll (id = ${maybePoll.get._id}) no longer acception votes"))
    }
  }
}
