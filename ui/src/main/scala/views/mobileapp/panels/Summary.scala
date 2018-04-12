package views.mobileapp.panels

import navigation.URIs._
import components.core.Implicits._
import components.core.Helpers._
import components.Components.{Layout, Button, Input, Misc, Modal}
import router.RoutingView
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import appstate.AppCircuit._
import appstate.MobileAppsSelector._
import appstate.ReviewsSelector._
import appstate.AuthSelector._
import appstate.SuggestionsSelector._
import apimodels.mobile.MobileApp
import appstate.{CreateReview, FetchReviews, CreatePoll, UpdateReview, FetchRelatedApps}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.{CreateReviewForm, CreatePollForm}
import java.time.LocalDate

object Summary {
  
  @dom
  def panel(reviews: Var[Seq[Review]], appId: String) = {

    val totalReviews = reviews.bind.length

    val avgRating =
      if (totalReviews == 0) totalReviews
      else
        reviews.value
          .foldLeft(0)((acc, curr) => acc + curr.rating) / totalReviews

    val rating =
      <div>{ for (i <- toBindingSeq(1 to avgRating)) yield { <Icon id={ "star" }/>.build.bind } }</div>

    //Fetch mobile app from server otherwise if user refreshes page this will be None...
    val genre =
      <div>Genre: { getMobileAppById(appId).map(_.genre).getOrElse("") }</div>;

    //NOTE: creating rating within this binding would prevent the whole div from
    // showing up when mounted in the Tile. Solutions:
    //1. wrap this binding inside an extra div (see Tiles above)
    //2. create a separate binding for the rating node (see uncommented code below)
    // TODO this issue needs to be investigated further
    <div>{ genre } { rating }</div>
  }
  
  //    @dom
//    val rating = {
//
//      val totalReviews = reviews.bind.length
//
//      val avgRating =
//        if (totalReviews == 0) totalReviews
//        else
//          reviews.value
//            .foldLeft(0)((acc, curr) => acc + curr.rating) / totalReviews
//
//      <div>{for (i <- toBindingSeq(1 to avgRating)) yield { <Icon id={"star"}/>.build.bind }}</div>
//    }
}