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
import apimodels.mobile.MobileApp
import appstate.{CreateReview, FetchReviews, CreatePoll, UpdateReview}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.{CreateReviewForm, CreatePollForm}
import java.time.LocalDate

object MobileAppPage {

  def view() = new RoutingView() {

    lazy val appId = routeParams(0)
    //lazy val app = Var[Option[MobileApp]](getMobileAppById(appId))
    val reviews = Var[Seq[Review]](Seq.empty)

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
                <Tile isPrimary={true} content={topBar.bind}/>
              )}/>,
              <Tile children={Seq(
                <Tile width={5} children={Seq(
                  <Tile isParent={true} isVertical={true} children={Seq(
                    <Tile isInfo={true} content={<div>{summary.bind}</div>}/>,
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

    @dom
    lazy val topBar =
    {
      <div style={"display: flex; justify-content: space-between"}>
        { appName.bind }
        { actions.bind }
      </div>;
    }

    @dom
    val reviewArea =
      <div>
        Reviews: { toBindingSeq(reviews.bind).map(x =>
        <div>
          <b>{ x.title } - { x.author.name } - { x.dateCreated.map(_.toString).getOrElse("just now") }</b>
          <p> { x.content }</p><br/>
        </div>).all.bind }
      </div>;

    @dom lazy val appName = {
      //Fetch mobile app from server otherwise if user refreshes page this will be None...
      <div>{ getMobileAppById(appId).map(_.name).getOrElse("") }</div>
    }

    @dom val summary = {
      
      val totalReviews = reviews.bind.length

      val avgRating =
        if (totalReviews == 0) totalReviews
        else
          reviews.value
            .foldLeft(0)((acc, curr) => acc + curr.rating) / totalReviews

      val rating = <div>{for (i <- toBindingSeq(1 to avgRating)) yield { <Icon id={"star"}/>.build.bind }}</div>
      
      //Fetch mobile app from server otherwise if user refreshes page this will be None...
      val genre = <div>Genre: { getMobileAppById(appId).map(_.genre).getOrElse("") }</div>;

      //NOTE: creating rating within this binding would prevent the whole div from
      // showing up when mounted in the Tile. Solutions:
      //1. wrap this binding inside an extra div (see Tiles above)
      //2. create a separate binding for the rating node (see uncommented code below)
      // TODO this issue needs to be investigated further
      <div>{genre} {rating}</div> 
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

    @dom
    val actions = {

      //TODO add icon to modal trigger ...icon={<Icon id="clipboard"/>}
      val open = pollPopUpIsOpen.bind
      val userReview = reviews.bind.find(_.author.userId == Some(getUserId()))
      <div style={"display: flex"}>
        <SimpleButton icon={<Icon id="heart"/>} label={"favorite"}/> 
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
            <CreatePollForm onSubmit={createPoll _}/> 
          </div>
        }/>
      </div>
    }

    @dom def reviewForm() = {
      <div> 
        <CreateReviewForm onSubmit={submitReview _}/> 
      </div>
    }

    def submitReview(title: String, content: String, rating: Int) = {
      dispatch(
        CreateReview(
          username = getUsername(),
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

    override def redirectCondition = getMobileAppById(appId) == None
    connect(reviews.value = getReviewsByApp(appId))(reviewSelector)
  }
}
