package views.reviews

import router.RoutingView
import com.thoughtworks.binding.{Binding, dom}, Binding.Var
import components.core.Implicits._
import components.core.Helpers._
import components.Components.Card
import appstate.FetchReviews
import org.scalajs.dom.raw.Event
import appstate.AuthSelector._
import appstate.AppCircuit._
import appstate.ReviewsSelector._
import apimodels.review.Review

object ReviewsPage {
  def view() = new RoutingView() {

    val reviews = Var[Seq[Review]](getUserReviews(getUserId))

    @dom
    override def element = {
      <div> { for { review <- toBindingSeq(reviews.bind) } yield {
        <div>
          <Card title={ review.title } subTitle={ review.rating.toString } content={
            <div> { review.content } </div>
          }/>
        </div>}}
      </div>
    }

    connect(reviews.value = getUserReviews(getUserId))(reviewSelector)
    // redierct condititon...
    dispatch(FetchReviews)
  }
}
