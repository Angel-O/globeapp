package views.mobileapp

import navigation.URIs._
import components.core.Implicits._
import components.core.Helpers._
import components.Components.{Layout, Button, Input, Misc}
import router.RoutingView
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import appstate.AppCircuit._
import appstate.MobileAppsSelector._
import appstate.ReviewsSelector._
import apimodels.mobileapp.MobileApp
import appstate.FetchReviews
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.CreateReviewForm

object MobileAppPage {
  
  def view() = new RoutingView() {

    lazy val appId = routeParams(0)
    lazy val app = Var[Option[MobileApp]](getMobileAppById(appId))
    val reviews = Var[Seq[Review]](Seq.empty)

    //TODO use pot data, feetch app by Id and store it in state
    // if fetching fails redirect to 404 or show error msg
    @dom override def element = {
      
      // dispatching here because appId is a lazy val
      // this avoids evaluating its value too early
      dispatch(FetchReviews(appId)) 

      val appName = app.bind.map(_.name).getOrElse("")
      val appDescription = app.bind.map(_.genre).getOrElse("")
      val reviewArea = <div>Reviews: { toBindingSeq(reviews.bind).map(x => <div>{x.content}</div>).all.bind }</div>
      val description = <div>Description: { appDescription }</div>
      val createReview =
        <div> <CreateReviewForm onSubmit={() => println("hello")}/> </div>
      val actions =
        <div>
          <SimpleButton icon={<Icon id="heart"/>} label={"favorite"}/>
          <SimpleButton icon={<Icon id="clipboard"/>} label={"create poll"}/>
        </div>

      val pageSkeleton =
        <div>
          <Tile isAncestor={true} children={Seq(
            <Tile isVertical={true} children={Seq(
              <Tile isParent={true} children={Seq(
                <Tile isPrimary={true} content={<div>{appName}</div>}/>
              )}/>,
              <Tile children={Seq(
                <Tile width={5} children={Seq(
                  <Tile isParent={true} isVertical={true} children={Seq(
                    <Tile isInfo={true} content={description}/>,
                    <Tile content={actions}/>,
                    <Tile content={createReview}/>
                  )}/>
                )}/>,
                <Tile isParent={ true } children={Seq(
                  <Tile isInfo={true} content={ reviewArea }/>
                )}/>
              )}/>
            )}/>
          )}/>
        </div>

      pageSkeleton
    }

    connect(app.value = getMobileAppById(appId))(mobileAppSelector)
    connect(reviews.value = getReviews())(reviewSelector)
  }
}
