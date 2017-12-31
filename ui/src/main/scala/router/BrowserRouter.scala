package router

import com.thoughtworks.binding.Binding.BindingSeq
import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

case class BrowserRouterBuilder() extends ComponentBuilder {
  def render = this 
  var routes: BindingSeq[RouteBuilder] = _ 
  
  private val router = Router()
  
  @dom def build = {
    
    val allRoutes = routes.all.bind
    allRoutes.foreach(x => { x.history_(router.history); Router.registerRoute(x, router) })
    
    val routerNode = router.build
    
    //this makes eclipse happy (calling bind on the router node would be enough)
    Constants(routerNode).all.bind.head.bind 
  }
}