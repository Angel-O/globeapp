package router

import com.thoughtworks.binding.dom
import components.core.ComponentBuilder

case class RouteBuilder() extends ComponentBuilder {
  def render = this

  var path: FragmentSeq = _
  var view: () => RoutingView = _

  // dynamic routes are built dynamically by the router hence why a dummy is built here
  @dom def build = dummy.build.bind
}
