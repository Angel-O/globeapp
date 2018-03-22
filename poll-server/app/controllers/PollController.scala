package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.poll.Poll
import apimodels.poll.Poll._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.toJson
import repos.PollRepository
import utils.Bson._

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
          .addOne(poll)
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
  def updatePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
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

  // TODO verify poll is still open
  // TODO use id for option???...options should be unique...
  // Note flatten(flatMap after parsing id is not enough because options and future do not compose...)
  def vote(pollId: String, optionId: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Voting")
    parseId(pollId)
      .flatMap(validId =>
        repository.getById(validId)
          .map(maybePoll => maybePoll
            .map(implicit poll => {
              addVote(req.user._id.get, optionId)
                .map(poll =>
                  repository
                    .updateOne(validId, poll)
                    .map(updated => Ok(toJson(updated))))
                .getOrElse(Future { BadRequest }) //poll option not found
            })
            .getOrElse(Future { NotFound }))).flatten //poll not found. 
      .recover({ case ex => { Logger.error("Invalid payload"); BadRequest } })
  }
}
