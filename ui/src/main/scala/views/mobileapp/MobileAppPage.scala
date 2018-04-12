package views.mobileapp

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
import views.mobileapp.panels._

object MobileAppPage {

  def view() = new RoutingView() {

    lazy val appId = routeParams(0)
    //lazy val app = Var[Option[MobileApp]](getMobileAppById(appId))
    val reviews = Var[Seq[Review]](Seq.empty)
    val relatedApps = Var[Seq[MobileApp]](Seq.empty)

    val pollPopUpIsOpen = Var(false)
    

    //TODO use pot data, fetch app by Id and store it in state
    // if fetching fails redirect to 404 or show error msg
    @dom override def element = {

      // dispatching here because appId is a lazy val
      // this avoids evaluating its value too early.
      // Ideally fetching needs to be done in the parent
      // component CUrrently it is done on both ...PICK one
      dispatch(FetchReviews(appId))

      val pageSkeleton =
        <div>
          <Tile isAncestor={true} children={Seq(
            <Tile isVertical={true} children={Seq(
              <Tile isParent={true} children={Seq(
                <Tile isPrimary={true} content={
                  <div> {TopBar.panel(appId, pollPopUpIsOpen, reviews, createPoll _).bind } </div>
                }/>
              )}/>,
              <Tile children={Seq(
                <Tile width={5} children={Seq(
                  <Tile isParent={true} isVertical={true} children={Seq(
                    <Tile isInfo={true} content={<div> {Summary.panel(reviews, appId).bind} </div>}/>,
                    <Tile content={
                      <div> {ReviewFormAndRelatedApps.panel(reviews, relatedApps, appId, submitReview _).bind} </div>
                    }/>
                  )}/>
                )}/>,
                <Tile isParent={true} children={Seq(
                  <Tile isInfo={true} content={<div> {ReviewList.panel(reviews).bind} </div>}/>
                )}/>
              )}/>
            )}/>
          )}/>
        </div>

      pageSkeleton
    }

    def submitReview(title: String, content: String, rating: Int) = {
      dispatch(
        CreateReview(username = getUsername(),
                     title = title,
                     content = content,
                     rating = rating,
                     mobileAppId = appId))
    }

    def createPoll(title: String,
                   content: String,
                   closingDate: LocalDate,
                   options: Seq[String]) = {
      dispatch(
        CreatePoll(title,
                   content,
                   appId,
                   createdBy = getUsername(),
                   closingDate,
                   options)
      )

      pollPopUpIsOpen.value = true //workaround to trigger change TODO fix modal once for good!!
      pollPopUpIsOpen.value = false
    }

    def update() = {
      reviews.value = getReviewsByApp(appId)
      relatedApps.value = getSuggestedMobileApps
    }
    override def redirectCondition = getMobileAppById(appId) == None
    multiConnect(update())(reviewSelector, suggestionSelector)
  }
}
