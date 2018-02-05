package router

import com.thoughtworks.binding.Binding.BindingSeq
import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

case class BrowserRouterBuilder() extends ComponentBuilder {
  def render = this 
  var routes: BindingSeq[RouteBuilder] = _ 
  var dynamicRoutes: BindingSeq[DynamicRouteBuilder] = _ 
  var baseUrl: String = _
  private lazy val router = Router(baseUrl)
  
  @dom def build = {
    
    val allStaticRoutes = routes.all.bind
    setHistoryAndRegisterRoutes(allStaticRoutes)
    
    val allDynamicRoutes = dynamicRoutes.all.bind
    registerDynamicRoutes(allDynamicRoutes)
    
    val routerNode = router.build
    
    //this makes eclipse happy (calling bind on the router node would be enough)
    Constants(routerNode).all.bind.head.bind 
  }
  
  private def setHistoryAndRegisterRoutes(routes: Seq[RouteBuilder]) = {
    routes.foreach(x => { x.history_(router.history); Router.registerRoute(x, router) })
  }
  
  private def registerDynamicRoutes(routes: Seq[DynamicRouteBuilder]) = {
    routes.foreach(x => Router.registerDynamicRoute(x, router))
  }
}