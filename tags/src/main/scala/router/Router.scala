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
   
  var dynamicRoutes: Seq[DynamicRouteBuilder] = Seq.empty
  val history = new BrowserHistory(this)
  
  private def root = {   
    // the initial page is not necessarily the baseUrl: 
    // the user might be on any page and hit refresh
    val rootPath = window.location.hash match {
        case "" => baseURL // no hash equal to home page
        case hash => hash.tail // exclude the hash
    }
    log.warn("root", rootPath)
    getRoute(rootPath) 
  }
  
  private lazy val activePage: Var[RoutingView] = Var(root)
  private def getRoute(path: String) = {
    dynamicRouteMatchFound(path) match{
        case true => getDynamicRoute(path)
        case _ => Router.DynamicNotFound.view
      }
  }
  
  private def dynamicRouteMatchFound(path: String) = {
    dynamicRoutes.exists( x => x.path.matchesUrl(path) )
  }
  
  private def getDynamicRoute(path: String) = {
    val matchingDynamicRoute = dynamicRoutes.find(x => x.path.matchesUrl(path)).getOrElse(Router.DynamicNotFound)
    val view = matchingDynamicRoute.view
    history.params = getParams(path)
    view.history = this.history
    view
  } 
  
  def getParams(path: String): Seq[String] = {
    val matchingDynamicRoute = dynamicRoutes.find(x => x.path.matchesUrl(path)).getOrElse(Router.DynamicNotFound)
    
    val params = 
      if(matchingDynamicRoute.path != Router.DynamicNotFound.path){
       matchingDynamicRoute.path.getRouteParams(path.tail).map(_.toString)
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
    require(dynamicRoutes.nonEmpty, "Cannot create router without routes")
    require(dynamicRoutes.map(_.path).toSet.size == dynamicRoutes.size, 
        s"Found duplicate routes: (${dynamicRoutes
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
  
  def addDynamicRoute(route: DynamicRouteBuilder) = dynamicRoutes = dynamicRoutes :+ route
  
  //TODO seems to be triggered many times....should this be a singleton???
  private val handleHashChange = (e: HashChangeEvent) => {
    val location = e.newURL
    val path = location.substring(location.indexOf("#")).toList match {
      case _ :: tail => tail.mkString("")
      case _ => s"#${Router.DynamicNotFound.path}" //TODO fix this: it's not doing anything
    }
    
    val route = getRoute(path)
    if (route == Router.DynamicNotFound.view){
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
  
  val DynamicNotFound = {
    import DynamicRoute._
    val route = new DynamicRouteBuilder()
    route.path = "/404".toFragment
    route.viewGenerator = () => new RoutingView(){ @dom override val element = dummy.build.bind }
    route
  }
  
  def registerDynamicRoute(route: DynamicRouteBuilder, router: Router) = router.addDynamicRoute(route)
  
  def apply(baseURL: String) = new Router(baseURL)
}