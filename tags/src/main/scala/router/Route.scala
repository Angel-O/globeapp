package router

import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

case class DynamicRouteBuilder() extends ComponentBuilder {
  def render = this
  import router.DynamicRoute._
  var path: FragmentSeq = _
  var view: RoutingView = _ 
  
  
  // dynamic routes are built dynamically by the router hence why a dummy is built here
  @dom def build = dummy.build.bind
}