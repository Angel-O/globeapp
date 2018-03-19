package controllers

import javax.inject.Inject
import play.api.Logger
import repos.PollRepository
import play.api.libs.json.Json._
import apimodels.poll.Poll
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.OFormat
import scala.concurrent.Future

class PollController @Inject() (
  scc:        SecuredControllerComponents,
  repository: PollRepository)
  extends SecuredController(scc) {

  def getAll = AuthenticatedAction.async {
    Logger.info("Fetching polls")
    repository.getAll.map(all => Ok(toJson(all)))
  }

  def getPoll(id: String) = AuthenticatedAction.async {
    Logger.info("Fetching poll")
    parseId(id).flatMap(validId => repository.getPoll(validId).map({
      case Some(poll) => Ok(toJson(poll))
      case None       => NotFound
    }))
  }

  def postPoll = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating poll")
    req.body.validate[Poll]
      .map(poll => repository.addPoll(poll).map(_ => Created))
      .getOrElse(Future { BadRequest("Invalid payload") })
  }

  def deletePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting poll")
    parseId(id).flatMap(validId => repository
      .deletePoll(validId)
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
        .updatePoll(validId, updated)
        .map({
          case Some(poll) => Ok(toJson(poll))
          case None       => NotFound
        })
    })
  }

  private def parseId(id: String) = {
    Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
  }

  private def newId = Some(BSONObjectID.generate.stringify)
}
