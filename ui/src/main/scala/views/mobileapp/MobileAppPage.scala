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
import apimodels.mobile.MobileApp
import appstate.{CreateReview, FetchReviews}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.CreateReviewForm

object MobileAppPage {

  def view() = new RoutingView() {

    lazy val appId = routeParams(0)
    //lazy val app = Var[Option[MobileApp]](getMobileAppById(appId))
    val reviews = Var[Seq[Review]](Seq.empty)

    //TODO use pot data, feetch app by Id and store it in state
    // if fetching fails redirect to 404 or show error msg
    @dom override def element = {

      // dispatching here because appId is a lazy val
      // this avoids evaluating its value too early.
      // Ideally fetching needs to be done in the parent
      // component CUrrently it is done on both ...PICK one
      dispatch(FetchReviews(appId))

      //Fetch mobile app from server otherwise if user refreshes page this will be None...
      val app = getMobileAppById(appId)

      val appName = <div>{ app.map(_.name).getOrElse("") }</div>
      val description =
        <div>Description: { app.map(_.genre).getOrElse("") }</div>

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
                <Tile isPrimary={true} content={appName}/>
              )}/>,
              <Tile children={Seq(
                <Tile width={5} children={Seq(
                  <Tile isParent={true} isVertical={true} children={Seq(
                    <Tile isInfo={true} content={description}/>,
                    <Tile content={actions}/>,
                    <Tile content={reviewForm.bind}/>
                  )}/>
                )}/>,
                <Tile isParent={true} children={Seq(
                  <Tile isInfo={true} content={reviewArea.bind}/>
                )}/>
              )}/>
            )}/>
          )}/>
        </div>

      pageSkeleton
    }

    //TODO investigate on this calling reviews.bind before the bindingSeq scope makes the whole thing fail
    // It's because of the way (recursion) Tiles are built: Solution(bind before the ancestor tile, or
    // bind in a reusable distinct component)
    @dom
    val reviewArea =
      <div>
        Reviews: { toBindingSeq(reviews.bind).map(x =>
        <div>
          <b>{ x.title } - { x.dateCreated.map(_.toString).getOrElse("just now") }</b>
          <p> { x.content }</p><br/>
        </div>).all.bind }
      </div>;

    @dom def reviewForm() = {
      <div> 
        <CreateReviewForm onSubmit={submitReview _}/> 
      </div>
    }

    def submitReview(title: String, content: String, rating: Int) = {
      dispatch(
        CreateReview(title = title,
                     content = content,
                     rating = rating,
                     mobileAppId = appId))
    }

    connect(reviews.value = getReviewsByApp(appId))(reviewSelector)
  }
}
