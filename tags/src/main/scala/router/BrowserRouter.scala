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
    routes.foreach(x => { 
      x.path = if(x.path != baseUrl) s"$baseUrl${x.path}" else x.path; x.history_(router.history)
      Router.registerRoute(x, router) 
    })
  }
  
  private def registerDynamicRoutes(routes: Seq[DynamicRouteBuilder]) = {
    import DynamicRoute._
    routes.foreach(x => { 
      x.path = baseUrl.tail / x.path
      Router.registerDynamicRoute(x, router) 
    })
  }
}