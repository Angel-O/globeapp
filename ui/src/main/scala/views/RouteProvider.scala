package views

import router.RoutingView
import com.thoughtworks.binding.dom
import components.Components.Implicits.{toBindingSeq, CustomTags2, _}
import navigation.URIs._
import router.DynamicRoute
import router.FragmentSeq

object RouteProvider {
  
  val dynamicRoutes = createDynamicRouteComponents(mapDynamicViewsToURIs)
 
  // mapping view components (the actual pages to display)
  private def mapDynamicViewsToURIs = {
    import router.DynamicRoute._
    
    //TODO use dynamic Route class...
    val routes: List[(FragmentSeq, RoutingView)] = List(
        HomePageURI.tail.toPath -> HomePage.view, 
        RegisterPageURI.tail.toPath -> RegistrationPage.view,
        SamplePageURI.tail.toPath -> SamplePage.view,
        UserPostURI -> UserEditPage.view
        )
        
    routes   
  }
  
  // creating wrappers around pages to provide routing capabilities
  private def createDynamicRouteComponents(routeMapping: List[(FragmentSeq, RoutingView)]) = {
    
    // yield uses a call back executed in another context where we cannot use the bind method
    // therfore we need to covert it to a binding sequence (under the hood the the component builder
    // will call the bind method...apparently)
    @dom
    val routes = (for((uri, view) <- toBindingSeq(routeMapping)) yield <DynamicRoute path={uri} view={view}/>)
    
    routes
  }
}