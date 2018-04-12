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

object ReviewList {
  def panel(reviews: Var[Seq[Review]]) = {
    
    @dom
    val reviewArea =
      <div>
        Reviews: { toBindingSeq(reviews.bind).map(x =>
        <div>
          <b>{ x.title } - { x.author.name } - { x.dateCreated.map(_.toString).getOrElse("just now") }</b>
          <p> { x.content }</p><br/>
        </div>).all.bind }
      </div>
    
    reviewArea
  }
}