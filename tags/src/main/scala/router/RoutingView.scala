package router

import components.core.Implicits._
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.dom
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.Binding.Constants
import components.core.ComponentBuilder

// there is no need to pass this via .ctor unless we want to hard code all the navigators
// and make them available to every possible routing view....
// We need to override memebers of this class on instantiation we might as well manually set
// the routes. TODO remove the navigators from c.tor if there is not a valid use case
class RoutingView(navigators: BrowserHistory => Unit*)
    extends ComponentBuilder {
  def render = {
    println("CARS") //TODO this is never called....fix it
    this
  }
  implicit var history: BrowserHistory = _
  def routeParams(index: Int) =
    Option(history).flatMap(_.getParams(index)).getOrElse("")

  def element: Binding[HTMLElement] = //TODO add support for pure html elements
    throw new IllegalArgumentException(
      "element method in RoutingView must be overridden")

  protected def redirectCondition: Boolean = false
  private def redirectToNotFoundIf(condition: => Boolean) = {
    if (condition) history.navigateToNotFound
  }

  @dom def build = {

    val viewBuilder = toComponentBuilder(element.bind).bind
    val viewElement = viewBuilder.build

    redirectToNotFoundIf(redirectCondition)

    //this makes eclipse happy (calling bind on the view element would be enough)
    Constants(viewElement).all.bind.head.bind
  }
}
