package router

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HashChangeEvent
import org.scalajs.dom.window
//import org.scalajs.dom.Location

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.dom

import components.core.ComponentBuilder
import components.core.Implicits._

case class Router private(baseURL: String, notFoundURL: String) extends ComponentBuilder {
   
  var routes: Seq[RouteBuilder] = Seq.empty
  val history = new BrowserHistory(this)
  
  // the browser router appends the base url to each path so to find the match we need to do the same
  lazy val notFoundRoute = routes.find(_.path.matchesUrl(s"$baseURL$notFoundURL")).get.view()
  
  private def root = {  
    // the initial page is not necessarily the baseUrl:
    // the user might be on any page and hit refresh
    val rootPath = window.location.hash match {
        case "" => baseURL // no hash equal to home page
        case hash => hash.tail // exclude the hash
    }
    log.warn("init location", rootPath)
    buildView(rootPath) 
  }
  
  private lazy val activePage: Var[RoutingView] = Var(root)
  
  private def buildView(path: String): RoutingView = {
    
    matchingRoute(path).map(route => {
      val view = route.view()
      history.params = getParams(path)
      view.history = this.history
      view
    }).getOrElse(notFoundRoute) 
  } 
  
  private def matchingRoute(path: String): Option[RouteBuilder] = {
    routes
    .find(x => x.path.matchesUrl(path))
  }
  
  def getParams(path: String): Seq[String] = {
     matchingRoute(path)
     .map(route => route.path.getRouteParams(path.tail).map(_.toString))
     .getOrElse(Seq.empty)
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
    require(notFoundURL != null, "Not found path required")
  }
  
  def navigateTo(path: String) = { 
    document.location.hash = path
  }
  
  def addRoute(route: RouteBuilder) = routes = routes :+ route
  
  private val handleHashChange = (e: HashChangeEvent) => {
    val location = e.newURL
    history.addToHistory(window.location.hash match {
        case "" => baseURL // no hash equal to home page
        case hash => hash.tail
    })
    val hashPosition = location.indexOf("#")
    val urlHasHash = hashPosition >= 0
    val destinationURI = location.substring(hashPosition).toList
    val path = (destinationURI, urlHasHash) match {
      case (_, false) => baseURL
      case (_ :: tail, _) => tail.mkString("")
      case _ => notFoundURL // is it ever triggered?
    } 
   
    activePage.value = buildView(path)
  }
  
  window.addEventListener("hashchange", handleHashChange)
}

//TODO store url params and query string...
case class BrowserHistory(val router: Router){
  
  private var history: Vector[String] = Vector.empty
  var params: Seq[String] = Seq.empty
  def navigateTo(path: String) = {
    // updating history on hash event beacuse the hash can change without
    // going through navigators
    params = router.getParams(path)
    router.navigateTo(router.baseURL + (if(path == router.baseURL) "" else path))
  }
  def navigateToNotFound = navigateTo(router.notFoundURL)
  def getParams(index: Int) = {
    if(index >= params.length) None else Some(params(index))
  }
  def addToHistory(path: String) = {
    history = history :+ path
  }
  def getHistory() = history
  def getLastVisited() = history.toList.reverse match {
    case first :: second :: _ =>
      if (second == router.baseURL) second
      else second.replace(router.baseURL, "") //removing base url
    case _ => router.baseURL
  }
}

private object Router {
  
  def registerRoute(route: RouteBuilder, router: Router) = router.addRoute(route)
  
  def apply(baseURL: String, notFoundURL: String) = new Router(baseURL, notFoundURL)
}