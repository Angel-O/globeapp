package router

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HashChangeEvent
import org.scalajs.dom.window
import org.scalajs.dom.Location

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.dom

import components.Components.Implicits.ComponentBuilder
import components.Components.Implicits.DummyBuilder
import components.Components.Implicits.autoBinding
import components.Components.Implicits.log
import org.scalajs.dom.raw.BeforeUnloadEvent
import com.sun.xml.internal.ws.client.sei.ValueSetter.ReturnValue

case class Router private() extends ComponentBuilder {
   
  var routes: Seq[RouteBuilder] = Seq.empty
  val history = new BrowserHistory(this)
  
  private def root = {
    //TODO change this, what if I need a router on a sub path?
    val rootPath = window.location.hash match {
        case "" => "/" // no hash equal to home page
        case hash => hash.tail //exclude the hash
    }
    getRoute(rootPath) //on page reload the user might be on an invalid page (getOrElse prevents throwing errors)
  }
  
  private lazy val activePage: Var[RoutingView] = Var(root)
  private def getRoute(path: String) = (routes.find(_.path == path).getOrElse(Router.NotFound)).view
   
  def render = activePage.value.render
  
  @dom def build: Binding[HTMLElement] = {
    validateRoutes
    
    routes.foreach(_.build)
    activePage.bind.build.bind
  }
  
  private def validateRoutes = {
    require(routes.nonEmpty, "Cannot create router without routes")
    require(routes.map(_.path).toSet.size == routes.size, 
        s"Found duplicate routes: (${routes
        .groupBy(identity)
        .collect { case (x,ys) if ys.lengthCompare(1) > 0 => x.path }
        .head})")
    require(routes.exists(_.path == "/"), "Root path ('/') required")
  }
  
  def navigateTo(path: String) = { 
    val newLocation = getRoute(path) 
    document.location.hash = path
    activePage.value = newLocation
  }
  
  def addRoute(route: RouteBuilder) = routes = routes :+ route
  
  private def setWindowLocation(path: String) = {
    val location = window.location
    location.hash_=(path)
  }
  
  private val handleHashChange = (e: HashChangeEvent) => {
    val location = e.newURL
    val path = location.substring(location.indexOf("#")).toList match {
      case _ :: tail => tail.mkString("")
      case _ => s"#${Router.NotFound.path}" //TODO fix this: it's not doing anything
    }
    val route = getRoute(path)
    if (route == Router.NotFound.view){
      //simulate redirect
      document.location.hash = Router.NotFound.path
    }
    activePage.value = route
  }
  
  window.addEventListener("hashchange", handleHashChange)
}

//TODO store params
case class BrowserHistory(val router: Router){
  
  var history: Vector[String] = Vector.empty
  def navigateTo(path: String) = {
    log.warn("Path", path)
    history = history :+ path
    router.navigateTo(path)
  }   
}

//TODO add ability to set custom 404 page
private object Router {
  
  val NotFound = {
    val route = new RouteBuilder() 
    route.path = "/404"
    route.view = new RoutingView(){ @dom override val element = dummy.build.bind }
    route
  }
  
  def registerRoute(route: RouteBuilder, router: Router) = router.addRoute(route)
  
  def apply() = new Router()
}