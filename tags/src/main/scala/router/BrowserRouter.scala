package router

import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Constants
import components.core.ComponentBuilder

case class Config(routes: BindingSeq[RouteBuilder],
                  baseUrl: String,
                  notFoundUrl: String)

case class BrowserRouterBuilder() extends ComponentBuilder {
  def render = this

  var config: Config = _

  private lazy val routes = config.routes
  private lazy val baseUrl = config.baseUrl
  private lazy val router = Router(baseUrl, config.notFoundUrl)

  @dom def build = {

    val allRoutes = routes.all.bind
    registerRoutes(allRoutes)

    val routerNode = router.build

    //this makes eclipse happy (calling bind on the router node would be enough)
    Constants(routerNode).all.bind.head.bind
  }

  private def registerRoutes(routes: Seq[RouteBuilder]) = {
    import DynamicRoute._
    //TODO this enforces constraint of base url having at least one char...
    routes.foreach(x => {
      x.path =
        if (x.path != baseUrl.tail.toPath) baseUrl.tail / x.path else x.path
      Router.registerRoute(x, router)
    })
  }
}
