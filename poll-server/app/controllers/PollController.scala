package controllers

import javax.inject.Inject
import play.api.Logger
import repos.PollRepository
import play.api.libs.json.Json
import apimodels.poll.{ Poll => ApiPoll, PollUploadModel }
import apimodels.poll.Formats._
import upickle.default._
import scala.concurrent.ExecutionContext.Implicits.global
import models.Poll, models.ConversionHelpers._
import reactivemongo.bson.BSONObjectID 
import play.api.libs.json.OFormat

case object JsonFormats{
  implicit val loginFormat: OFormat[PollUploadModel] = Json.format[PollUploadModel] //todo can is just use upickle...
}
class PollController @Inject()(scc: SecuredControllerComponents,
                                    repository: PollRepository)
    extends SecuredController(scc) {

  import JsonFormats._
  def getAll = AuthenticatedAction.async {
    Logger.info("Fetching polls")
    repository.getAll.map(all => Ok(write(all.map(_.toApi))))
  }
  
  def getPoll(id: String) = AuthenticatedAction.async {
    Logger.info("Fetching poll")
    repository.getPoll(BSONObjectID.parse(id).get).map({
      case Some(mobileApp) => Ok(write(mobileApp.toApi))
      case None => NotFound
    })
  }

  def postPoll = AuthenticatedAction.async(parse.json) { req =>
    val payload: String = Json.stringify(req.body)
    val ff = req.body.validate[PollUploadModel]
    val apiPoll = read[ApiPoll](payload)
    repository.addPoll(apiPoll.toModel).map(_ => Created)
  }

  //TODO fix this
  def deleteApp(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Deleting mobile app")
    val id: String = req.body.as[String]
    repository
      .deletePoll(BSONObjectID.parse(id).get)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp.toApi))
        case None            => NotFound
      })
  }

  //TODO fix this
  def updatePoll(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating mobile app")
    //val payload: String = Json.stringify(req.body)
    val id = req.user.id
    val updated = req.body.validate[Poll].get //read[User](payload)
    repository
      .updatePoll(BSONObjectID.parse(id).get, updated)
      .map({
        case Some(mobileApp) => Ok(write(mobileApp.toApi))
        case None            => NotFound
      })
  }
}
