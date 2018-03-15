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

import apimodels.review.Review
import java.time.LocalDate

// Model
case class Reviews(reviews: Seq[Review])
case object Reviews {
  def apply() = new Reviews(Seq.empty)
}

// Primary actions
case class FetchReviews(mobileAppId: String) extends Action
//case class FetchReview(reviewId: String) extends Action
case class CreateReview(userId: String, content: String, mobileAppId: String)
    extends Action

// Secondary actions
case class ReviewsFetched(reviews: Seq[Review]) extends Action

// Action handler
class ReviewHandler[M](modelRW: ModelRW[M, Seq[Review]])
    extends ActionHandler(modelRW)
    with ReviewEffects {
  override def handle = {
    case FetchReviews(mobileAppId) =>
      effectOnly(fetchReviewsEffect(mobileAppId))
    case ReviewsFetched(reviews) => updated(reviews)
    //case CreateReview(userId, content, mobileAppId) => ???
  }
}

// Effects
trait ReviewEffects extends Push {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import upickle.default._
  import utils.api._
  import diode.{Effect, NoAction}
  import config._

  //TODO implement real api calls
  import mock.ReviewApi._

  def fetchReviewsEffect(reviewId: String) = {
    Effect(Future { 1 }.map(_ => ReviewsFetched(getAll)))
  }

}

// Selector
object ReviewsSelector extends ReadConnect[AppModel, Seq[Review]] {
  def getReviews() = model
  def getReviewById(id: String) = getReviews().find(_._id == id)
  def getUserReviews(userId: String) = getReviews.find(_.userId == userId)

  val cursor = AppCircuit.reviewSelector
  val circuit = AppCircuit
}
