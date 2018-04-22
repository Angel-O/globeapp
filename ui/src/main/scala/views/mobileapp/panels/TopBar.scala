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
import appstate.{CreateReview, FetchReviews, CreatePoll, UpdateReview, FetchRelatedApps, AddAppToFavorites}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.{CreateReviewForm, CreatePollForm}
import java.time.LocalDate

object TopBar {
  
    @dom
    def panel(
        appId: String,
        pollPopUpIsOpen: Var[Boolean], 
        reviews: Var[Seq[Review]],
        createPoll: (String, String, LocalDate, Seq[String]) => Unit) = {

      val open = pollPopUpIsOpen.bind
      val userReview = reviews.bind.find(_.author.userId == Some(getUserId()))
      
      //Fetch mobile app from server otherwise if user refreshes page this will be None...
      val appName = getMobileAppById(appId).map(_.name).getOrElse("")
      
      def addtoFavorites() = dispatch(AddAppToFavorites(appId))
      
      <div style={"display: flex; justify-content: space-between"}>
        <div> { appName } </div>
        <div style={"display: flex"}>
          <SimpleButton icon={<Icon solid={Some(false)} id="heart"/>} label={"favorite"} onClick={addtoFavorites _}/> 
          {toBindingSeq(userReview).map(review => {
            def updateReview(title: String, content: String, rating: Int) = 
            dispatch(UpdateReview(review._id.get, title, content, rating));

          <div>
            <PageModal label={"update review"} isOpen={false} content={
              <div>
                <CreateReviewForm submitLabel={"Update review"} title={review.title} 
                content={review.content} rating={review.rating} onSubmit={updateReview _}/> 
              </div>
            }/>
          </div>}).all.bind}
          <PageModal label={"create poll"} isOpen={open} content={
            <div>
              <CreatePollForm onSubmit={createPoll}/> 
            </div>
          }/>
        </div>
      </div>
    }
}