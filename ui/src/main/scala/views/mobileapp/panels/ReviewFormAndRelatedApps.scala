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
import appstate.{
  CreateReview,
  FetchReviews,
  CreatePoll,
  UpdateReview,
  FetchRelatedApps
}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.{CreateReviewForm, CreatePollForm}
import java.time.LocalDate
import hoc.panel.AppsPanel

object ReviewFormAndRelatedApps {

  @dom
  def panel(reviews: Var[Seq[Review]],
            relatedApps: Var[Seq[MobileApp]],
            appId: String,
            submitReview: (String, String, Int) => Unit) = {

    // note selector is not enough because does not use a binding...
    // a binding is necessary to update correctly after a review has been created
    val userHasVoted = reviews.bind
      .find(review =>
        review.author.userId == Some(getUserId()) && review.mobileAppId == appId)
      .nonEmpty

    if (userHasVoted) {
      dispatch(FetchRelatedApps(appId))
    }

    val panel =
      <div> { if(userHasVoted) { 
        <div>
        { relatedAppsPanel(relatedApps.bind).bind }
        </div> } else {
        <div>
          <CreateReviewForm onSubmit={submitReview}/>
        </div> } }    
      </div>

    panel
  }

  def relatedAppsPanel(relatedApps: Seq[MobileApp]) = {

    @dom val panel =
      <AppsPanel apps={relatedApps} header="Related apps"/>

    panel
  }
}
