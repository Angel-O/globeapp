package router

import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

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