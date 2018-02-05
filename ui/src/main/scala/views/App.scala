package views

import com.thoughtworks.binding.{dom, Binding}
import org.scalajs.dom.{document, Node}
import components.Components.Implicits.CustomTags2

object App {
  
  def main(args: Array[String]): Unit = {
    
    val routes = RouteProvider.routes
    val dynamicRoutes = RouteProvider.dynamicRoutes
    
    @dom def render = {
        
      // build the router (this could just be wrapped into a div, 
      // to handle building and binding automatically rather than 
      // calling them manually)
      //TODO base Url not working now
      <BrowserRouter baseUrl={"/"} routes={ routes.bind } dynamicRoutes={dynamicRoutes.bind}/>.build.bind
		}
    
    // mount the App       
    dom.render(document.body, render.asInstanceOf[Binding[Node]]) 
  }
}

