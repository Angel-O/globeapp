package views

import com.thoughtworks.binding._
import org.scalajs.dom.document
import org.scalajs.dom.Node

// implicit conversions and helper methods
import components.Components.Implicits._
// use for tag registration at runtime
import hoc.form.RegistrationFormBuilder
// use for tag registration at compile time
import hoc.form._
// use for bulk registration at compile time and run time
import Tags._


import router.{BrowserHistory, RoutingView}
import navigation.Navigators._ 
import navigation.URIs._
import org.scalajs.dom.raw.HTMLElement
import scala.xml.Elem

object App {
  
  def main(args: Array[String]): Unit = {
    
    @dom def render = {
      
      // mapping view components (the actual pages to display)
      val routeMapping = mapViewsToURIs()  
      
      // creating wrappers around pages to provide routing capabilities
      val routes = createRouteComponents(routeMapping)
       
      // build the router (this could just be wrapped into a div, 
      // to handle building and binding automatically rather than 
      // calling them manually)
      <BrowserRouter routes={ routes.bind } />.build.bind
		}
    
    // mount the App
    dom.render(document.body, render.asInstanceOf[Binding[Node]])   
  }
  
  // TODO move this logic to a Factory class
  private def mapViewsToURIs(): List[(String, RoutingView)] = {
    
    import HomePage.{ homePage => home }
    val homePage = home 
     
    //val form = customTags.RegistrationForm()
    val helloPage = new RoutingView() {
      //WORKS with Macros!!!
      @dom override def element = <div><MyComponent foo="Hi mate" inner={<p>Hello</p>}/></div>
    }
    
    val registerPage = new RoutingView() {
        @dom override def element = 
          <ModalCard 
						label={"Launch form"} 
						title={"Register now"} 
						content={<div><RegistrationForm onSubmit={navigateToHello _} onClick={() => println("CLICKED")}/></div>}
						onSave={navigateToHome _}/>.build.bind }
      
    val routes = List(
        HomePageURI -> homePage, 
        RegisterPageURI -> registerPage,
        HelloPageURI -> helloPage)
        
     routes
  } 
  
  // TODO move this logic to a Factory class
  private def createRouteComponents(routeMapping: List[(String, RoutingView)]) = {
    
    // yield uses a call back executed in another context where we cannot use the bind method
    // therfore we need to covert it to a binding sequence (under the hood the the component builder
    // will call the bind method...apparently)
    @dom
    val routes = (for((uri, view) <- toBindingSeq(routeMapping)) 
                    yield <Route 
														path={uri} view={view}/>)
    
    routes
  }
}

