package router

import com.thoughtworks.binding.Binding.BindingSeq
import components.Components.Implicits.ComponentBuilder
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants

case class BrowserRouterBuilder() extends ComponentBuilder {
  def render = this
  var routes: BindingSeq[RouteBuilder] = _
  var baseUrl: String = _
  private lazy val router = Router(baseUrl)

  @dom def build = {

    val allRoutes = routes.all.bind
    registerRoutes(allRoutes)

    val routerNode = router.build

    //this makes eclipse happy (calling bind on the router node would be enough)
    Constants(routerNode).all.bind.head.bind
  }

  private def registerRoutes(routes: Seq[RouteBuilder]) = {
    import DynamicRoute._
    //TODO this enforces contraint of base url having at least one char...
    routes.foreach(x => {
      x.path = if (x.path != baseUrl.tail.toPath) baseUrl.tail / x.path else x.path
      Router.registerRoute(x, router)
    })
  }
}
