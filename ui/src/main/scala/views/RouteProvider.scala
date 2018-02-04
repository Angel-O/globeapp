package views

import router.RoutingView
import com.thoughtworks.binding.dom
import components.Components.Implicits.{toBindingSeq, CustomTags2, _}
import navigation.URIs._

object RouteProvider {
  
  val routes = createRouteComponents(mapViewsToURIs)
  
  // mapping view components (the actual pages to display)
  private def mapViewsToURIs(): List[(String, RoutingView)] = {
    
    import HomePage.{ view => home }
    import UserEditPage.{ view => userEdit }
    import RegistrationPage.{ view => register }
    import SamplePage.{ view => sample }
     
    //val form = customTags.RegistrationForm()
          
    val routes = List(
        HomePageURI -> home, 
        RegisterPageURI -> register,
        UserEditPageURI -> userEdit,
        s"$UserEditPageURI/:username" -> userEdit, //TODO !!!
        SamplePageURI -> sample)
        
     routes
  } 
  
  // creating wrappers around pages to provide routing capabilities
  private def createRouteComponents(routeMapping: List[(String, RoutingView)]) = {
    
    // yield uses a call back executed in another context where we cannot use the bind method
    // therfore we need to covert it to a binding sequence (under the hood the the component builder
    // will call the bind method...apparently)
    @dom
    val routes = (for((uri, view) <- toBindingSeq(routeMapping)) yield <Route path={uri} view={view}/>)
    
    routes
  }
}