package router

import components.Components.Implicits.{ toComponentBuilder , ComponentBuilder}
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.dom
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.Binding.Constants

// there is no need to pass this via .ctor unless we want to hard code all the navigators
// and make them available to every possible routing view....
// We need to override memebers of this class on instantiation we might as well manually set
// the routes. TODO remove the navigators from c.tor if there is not a valid use case
class RoutingView(navigators: BrowserHistory => Unit*) extends ComponentBuilder {
  def render = this
  implicit var history: BrowserHistory = _
  def element: Binding[HTMLElement] = //TODO add support for pure html elements
    throw new IllegalArgumentException("element method in RoutingView must be overridden")
  
  @dom def build: Binding[HTMLElement] = {
   
    val viewBuilder = toComponentBuilder(element.bind).bind
    val viewElement = viewBuilder.build
    
    //this makes eclipse happy (calling bind on the view element would be enough)
    Constants(viewElement).all.bind.head.bind 
  }
}