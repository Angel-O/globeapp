package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.review.Review
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Json._
import repos.ReviewRepository
import utils.Bson._
import utils.Date._

class ReviewController @Inject() (
  scc:        SecuredControllerComponents,
  repository: ReviewRepository)
  extends SecuredController(scc) {

  def getAll = Action.async {
    Logger.info("Fetching reviews")
    repository.getAll.map(reviews => Ok(toJson(reviews)))
  }

  def getReview(id: String) = Action.async {
    Logger.info("Fetching review")
    parseId(id)
      .flatMap(validId =>
        repository
          .getById(validId))
          .map({
            case Some(review) => Ok(toJson(review))
            case None            => NotFound
          })
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def getReviews(mobileAppId: String) = Action.async {
    Logger.info(s"Fetching reviews for mobile app (id = ${mobileAppId})")
    parseId(mobileAppId)
      .flatMap(validId =>
        repository
          .getAllByApp(mobileAppId)
          .map(reviews => Ok(toJson(reviews))))
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
  }

  def postReview = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Creating review")
    req.body.validate[Review]
      .map(uploadModel => {
        repository
          .getByKey(uploadModel.mobileAppId, req.user._id.get)
          .flatMap({
            case Some(_) =>
              Future(BadRequest(
                s"User with id ${uploadModel.userId} have already created review for " +
                  s"app with id ${uploadModel.mobileAppId}"))
            case None => {
              val app = uploadModel.copy(_id = newId, dateCreated = newDate, userId = req.user._id)
              repository
                .addOne(app)
                .map(id => Created(id))
                .recover({ case ex => Logger.error(ex.getMessage); BadRequest })
            }
          })
      })
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }

  def deleteReview(id: String) = AuthenticatedAction.async { req =>
    Logger.info("Deleting review")
    parseId(id)
      .flatMap(validId =>
        repository
          .getByIdAndUser(validId, req.user._id.get) //verifying user who is trying to delete the review is actually the author
          .flatMap({
            case None => Future{ NotFound }
            case Some(_) => 
              repository
                .deleteOne(validId)
                .collect({ case Some(review) => Ok(toJson(review)) })
           })
      .recover({ case ex => Logger.error(ex.getMessage); BadRequest }))
  }

  // See deleteReview for an alternative on how to check that the user is the author
  def updateReview(id: String) = AuthenticatedAction.async(parse.json) { req =>
    Logger.info("Updating review")
    req.body.validate[Review]
      .map(uploadModel =>
        parseId(id)
          .flatMap(validId =>
            repository
              .getById(validId)
              .map(maybeReview => maybeReview.map(review => Some(review.userId) == req.user._id))
              .flatMap({
                case None        => Future { NotFound }
                case Some(false) =>
                  Future {
                    Logger.warn(s"User (id: ${req.user._id}) trying to update someone" +
                      s"else's review. (reviewId: ${validId})")
                    Forbidden
                  }
                case Some(true) =>
                  repository
                    .updateOne(validId, uploadModel)
                    .collect({ case Some(review) => Ok(toJson(review)) })
              }))
          .recover({ case ex => Logger.error(ex.getMessage); BadRequest }))
      .getOrElse({ Logger.error("Invalid payload"); Future(BadRequest) })
  }
}
