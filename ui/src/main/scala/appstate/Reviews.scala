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
case class CreateReview(title: String, content: String, rating: Int, mobileAppId: String)
    extends Action

// Secondary actions
case class ReviewsFetched(reviews: Seq[Review]) extends Action
case class ReviewCreated(review: Review) extends Action

// Action handler
class ReviewHandler[M](modelRW: ModelRW[M, Seq[Review]])
    extends ActionHandler(modelRW)
    with ReviewEffects {
  override def handle = {
    case FetchReviews(mobileAppId) =>
      effectOnly(fetchReviewsEffect(mobileAppId))
    case ReviewsFetched(reviews) => updated(reviews)
    case CreateReview(title, content, rating, mobileAppId) =>
      effectOnly(createReviewEffect(title, content, rating, mobileAppId))
    case ReviewCreated(review) => updated(review +: value) // or fetch again??...no userId...
  }
}

// Effects
trait ReviewEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._
  import diode.{Effect, NoAction}
  import config._

  import play.api.libs.json.Json._

  def fetchReviewsEffect(reviewId: String) = {
    Effect(Get(url = s"$REVIEW_SERVER_ROOT/api/reviews").map(xhr => ReviewsFetched(read[Seq[Review]](xhr.responseText))))
  }

  def createReviewEffect(title: String, content: String, rating: Int, mobileAppId: String) = {
    val review = Review(title = title, content = content, rating = rating, mobileAppId = mobileAppId)
    Effect(Post(url = s"$REVIEW_SERVER_ROOT/api/reviews", payload = write(review))
        .map(xhr => ReviewCreated(review.copy(_id = Some(xhr.responseText))))) //TODO recover
  }
  
  // def testEffect(review:Review) = {
  //   Effect(Post(url = s"$REVIEW_SERVER_ROOT/api/reviews", payload = write(review))
  //       .map(xhr => MatchingUsernamesCount(xhr.responseText.toInt))
  //       .recover({ case _ => VerifyUsernameAlreadyTakenFailed }))
  // }
}

// Selector
object ReviewsSelector extends ReadConnect[AppModel, Seq[Review]] {
  def getReviews() = model
  def getReviewById(id: String) = getReviews().find(_._id == Some(id))
  def getReviewsByApp(mobileAppId: String) = getReviews().filter(_.mobileAppId == mobileAppId)
  def getUserReviews(userId: String) = getReviews.find(_.userId == Some(userId))

  val cursor = AppCircuit.reviewSelector
  val circuit = AppCircuit
}
