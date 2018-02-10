package router

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HashChangeEvent
import org.scalajs.dom.window
//import org.scalajs.dom.Location

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.dom

import components.Components.Implicits.ComponentBuilder
import components.Components.Implicits.log

case class Router private(baseURL: String) extends ComponentBuilder {
   
  var routes: Seq[RouteBuilder] = Seq.empty
  val history = new BrowserHistory(this)
  
  private def root = {   
    // the initial page is not necessarily the baseUrl: 
    // the user might be on any page and hit refresh
    val rootPath = window.location.hash match {
        case "" => baseURL // no hash equal to home page
        case hash => hash.tail // exclude the hash
    }
    log.warn("init location", rootPath)
    getRoute(rootPath) 
  }
  
  private lazy val activePage: Var[RoutingView] = Var(root)
  private def getRoute(path: String) = {
    matchingRouteFound(path) match{
        case true => buildView(path)
        case _ => Router.NotFound.view
      }
  }
  
  private def matchingRouteFound(path: String) = {
    routes.exists( x => x.path.matchesUrl(path) )
  }
  
  private def buildView(path: String) = {
    val view = matchingRoute(path).view
    history.params = getParams(path)
    view.history = this.history
    view
  } 
  
  private def matchingRoute(path: String) = {
    routes.find(x => x.path.matchesUrl(path)).getOrElse(Router.NotFound)
  }
  
  def getParams(path: String): Seq[String] = {
    val route = matchingRoute(path)
    
    val params = 
      if(route.path != Router.NotFound.path){
       route.path.getRouteParams(path.tail).map(_.toString)
      } 
      else{ Seq.empty[String] }
    
     params
  }
   
  def render = activePage.value.render
  
  @dom def build: Binding[HTMLElement] = {
    validateRoutes  
    activePage.bind.build.bind
  }
  
  private def validateRoutes = {
    require(routes.nonEmpty, "Cannot create router without routes")
    require(routes.map(_.path).toSet.size == routes.size, 
        s"Found duplicate routes: (${routes
        .groupBy(identity)
        .collect { case (x,ys) if ys.lengthCompare(1) > 0 => x.path }
        .head})")
        
    require(baseURL != null, "Base path required") //TODO set default to "/" if not specified
    require(!baseURL.isEmpty, "Base path required")
  }
  
  def navigateTo(path: String) = { 
    val newLocation = getRoute(path) 
    document.location.hash = path
    activePage.value = newLocation
  }
  
  def addRoute(route: RouteBuilder) = routes = routes :+ route
  
  //TODO seems to be triggered many times....should this be a singleton???
  private val handleHashChange = (e: HashChangeEvent) => {
    val location = e.newURL
    val hashPosition = location.indexOf("#")
    val urlHasHash = hashPosition >= 0
    val path = 
      if(urlHasHash) {
      location.substring(hashPosition).toList match {
        case _ :: tail => tail.mkString("")
        case _ => s"#${Router.NotFound.path}" } 
      }
      else{ baseURL }
    
    val route = getRoute(path)
    if (route == Router.NotFound.view){
      //simulate redirect
      //document.location.hash = Router.NotFound.path
      e.stopImmediatePropagation()
    }
    activePage.value = route
  }
  
  window.addEventListener("hashchange", handleHashChange)
}

//TODO store url params and query string...
case class BrowserHistory(val router: Router){
  
  private var history: Vector[String] = Vector.empty
  var params: Seq[String] = Seq.empty
  def navigateTo(path: String) = {
    history = history :+ path
    params = router.getParams(path)
    router.navigateTo(router.baseURL + (if(path == router.baseURL) "" else path))
  }
  def getParams = params //TODO return immutable copy
}

//TODO add ability to set custom 404 page
private object Router {
  
  val NotFound = {
    import DynamicRoute._
    val route = new RouteBuilder()
    route.path = "/404".toFragment
    route.view = new RoutingView(){ @dom override val element = dummy.build.bind }
    route
  }
  
  def registerRoute(route: RouteBuilder, router: Router) = router.addRoute(route)
  
  def apply(baseURL: String) = new Router(baseURL)
}