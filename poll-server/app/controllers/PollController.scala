package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.poll.Poll
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
    parseId(id).flatMap(
      validId =>
        repository
          .getById(validId)
          .map({
            case Some(poll) => Ok(toJson(poll))
            case None       => NotFound
          }))
  }

  def postPoll = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating poll")
    req.body
      .validate[Poll]
      .map(poll => repository.addOne(poll).map(_ => Created))
      .getOrElse(Future { BadRequest("Invalid payload") })
  }

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
}
