package router

import com.thoughtworks.binding.Binding.BindingSeq
import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

case class BrowserRouterBuilder() extends ComponentBuilder {
  def render = this
  var dynamicRoutes: BindingSeq[DynamicRouteBuilder] = _
  var baseUrl: String = _
  private lazy val router = Router(baseUrl)

  @dom def build = {

    val allDynamicRoutes = dynamicRoutes.all.bind
    registerDynamicRoutes(allDynamicRoutes)

    val routerNode = router.build

    //this makes eclipse happy (calling bind on the router node would be enough)
    Constants(routerNode).all.bind.head.bind
  }

  private def registerDynamicRoutes(routes: Seq[DynamicRouteBuilder]) = {
    import DynamicRoute._
    //TODO this enforces contraint of base url having at least one char...
    routes.foreach(x => {
      x.path = if (x.path != baseUrl.tail.toPath) baseUrl.tail / x.path else x.path
      Router.registerDynamicRoute(x, router)
    })
  }
}
