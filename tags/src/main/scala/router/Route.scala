package router

import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

//TODO use sealed trait
//TODO remove distinction between dynamic and static routes
case class RouteBuilder() extends ComponentBuilder {
  def render = this
  var path: String = _
  var history: BrowserHistory = _
  var view: RoutingView = _
  
  def history_(history: BrowserHistory) = this.history = history
  
  @dom def build = {
    view.history = this.history
    val viewBuilder = view.build
    
    //this makes eclipse happy (calling bind on the view builder would be enough)
    Constants(viewBuilder).all.bind.head.bind 
  }
}

case class DynamicRouteBuilder() extends ComponentBuilder {
  def render = this
  import router.DynamicRoute._
  var path: FragmentSeq = _
  var viewGenerator: () => RoutingView = _ 
  
  
  // dynamic routes are built dynamically by the router
  // based on params...dummy is enough
  @dom def build = dummy.build.bind
}