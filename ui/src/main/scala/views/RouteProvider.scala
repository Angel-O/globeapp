package views

import router.RoutingView
import com.thoughtworks.binding.dom
import components.core.Implicits._
import components.core.Helpers._
import components.Components.Router
import navigation.URIs._
import router.DynamicRoute._
import register._
import login._
import home._
import catalog._
import poll._

object RouteProvider {

  val routes = createRouteComponents(mapViewsToURIs)

  // mapping view components (the actual pages to display)
  private def mapViewsToURIs = {

    //TODO use dynamic Route class...
    val mapping: List[(Path, () => RoutingView)] = List(
      HomePageURI.tail.toPath -> HomePage.view _,
      RegisterPageURI.tail.toPath -> RegistrationPage.view _,
      LoginPageURI.tail.toPath -> LoginPage.view _,
      SamplePageURI.tail.toPath -> SamplePage.view _,
      CatalogPageURI.tail.toPath -> CatalogPage.view _,
      PollsPageURI.tail.toPath -> PollsPage.view _
      //UserPostURI -> UserEditPage.view _
    )

    mapping
  }

  // creating wrappers around pages to provide routing capabilities
  private def createRouteComponents(
      routeMapping: List[(Path, () => RoutingView)]) = {

    // yield uses a call back executed in another context where we cannot use the bind method
    // therfore we need to covert it to a binding sequence (under the hood the the component builder
    // will call the bind method...apparently)
    @dom
    val routes = (for ((uri, view) <- toBindingSeq(routeMapping))
      yield <Route path={uri} view={view}/>)

    routes
  }
}
