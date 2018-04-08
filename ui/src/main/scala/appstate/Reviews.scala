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

import apimodels.review.{Review, Author}
import java.time.LocalDate

// Model
case class Reviews(reviews: Seq[Review])
case object Reviews {
  def apply() = new Reviews(Seq.empty)
}

// Primary actions
case class FetchReviews(mobileAppId: String) extends Action
//case class FetchReview(reviewId: String) extends Action
case class CreateReview(username: String, title: String, content: String, rating: Int, mobileAppId: String)
    extends Action
case class UpdateReview(reviewId: String, title: String, content: String, rating: Int)
    extends Action

// Secondary actions
case class ReviewsFetched(reviews: Seq[Review]) extends Action
case class ReviewCreated(review: Review) extends Action
case class ReviewUpdated(review: Review) extends Action

// Action handler
class ReviewHandler[M](modelRW: ModelRW[M, Seq[Review]])
    extends ActionHandler(modelRW)
    with ReviewEffects {
  override def handle = {
    case FetchReviews(mobileAppId) =>
      effectOnly(fetchReviewsEffect(mobileAppId))
    case ReviewsFetched(reviews) => updated(reviews)
    case CreateReview(username, title, content, rating, mobileAppId) =>
      effectOnly(createReviewEffect(username, title, content, rating, mobileAppId))
    case ReviewCreated(review) => updated(review +: value) // or fetch again??...no userId...
    case UpdateReview(reviewId, title, content, rating) => {
      value.find(_._id == Some(reviewId))
      .map(review => effectOnly(updateReviewEffect(review, title, content, rating)))
      .getOrElse(noChange)
    }
    case ReviewUpdated(review) => { 
      (for {
        oldReview <- value.find(_._id == review._id)
        index <- Some(value.indexOf(oldReview))
      } yield(updated(value.updated(index, review))))
      .getOrElse(noChange)
    }
  }
}

// Effects
trait ReviewEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import utils.api._, utils.redirect._
  import diode.{Effect, NoAction}
  import config._

  import play.api.libs.json.Json._

  def fetchReviewsEffect(reviewId: String) = {
    Effect(Get(url = s"$REVIEW_SERVER_ROOT/api/reviews").map(xhr => ReviewsFetched(read[Seq[Review]](xhr.responseText))))
  }

  def createReviewEffect(username: String, title: String, content: String, rating: Int, mobileAppId: String) = {
    val review = Review(author = Author(name = username), title = title, content = content, rating = rating, mobileAppId = mobileAppId)
    Effect(Post(url = s"$REVIEW_SERVER_ROOT/api/reviews", payload = write(review))
      .map(xhr => ReviewCreated(review.copy(_id = Some(xhr.responseText))))
      .redirectOnFailure)
  }

  def updateReviewEffect(oldReview: Review, title: String, content: String, rating: Int) = {
    val review = oldReview.copy(title = title, content = content, rating = rating) 
    Effect(Put(url = s"$REVIEW_SERVER_ROOT/api/reviews/${oldReview._id.get}", payload = write(review))
      .map(xhr => ReviewUpdated(read[Review](xhr.responseText)))
      .redirectOnFailure)
  }
}

// Selector
object ReviewsSelector extends ReadConnect[AppModel, Seq[Review]] {
  def getReviews() = model.sortBy(_.dateCreated.map(identity))
  def getReviewById(id: String) = getReviews().find(_._id == Some(id))
  def getReviewsByApp(mobileAppId: String) = getReviews().filter(_.mobileAppId == mobileAppId)
  def getUserReviews(userId: String) = getReviews.find(_.author.userId == Some(userId))
  def getUserHasAlreadyVoted(userId: String, mobileAppId: String) = { 
    model
    .filter(_.mobileAppId == mobileAppId)
    .find(_.author.userId == Some(userId))
    .fold(false)(_ =>true)
  }

  val cursor = AppCircuit.reviewSelector
  val circuit = AppCircuit
}
