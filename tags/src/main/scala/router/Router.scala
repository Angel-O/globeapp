package router

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.HashChangeEvent
//import org.scalajs.dom.raw.Event
import org.scalajs.dom.window
//import org.scalajs.dom.Location

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.dom

import components.Components.Implicits.ComponentBuilder
//import components.Components.Implicits.DummyBuilder
//import components.Components.Implicits.autoBinding
import components.Components.Implicits.log
//import org.scalajs.dom.raw.BeforeUnloadEvent
//import com.sun.xml.internal.ws.client.sei.ValueSetter.ReturnValue

case class Router private(baseURL: String) extends ComponentBuilder {
   
  var routes: Seq[RouteBuilder] = Seq.empty
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
    println("NAVIGATING TO", routes.find(_.path == path))
    val route = routes.find(_.path == path)
    route match {
    case Some(x) => x.view 
    case None => dynamicRouteMatchFound(path) match{
        case true => getDynamicRoute(path)
        case _ => Router.NotFound.view
      }
    }   
  }
  
  // private def dynamicRouteMatchFoundOLD(path: String) = {
  //   dynamicRoutes.exists( x => path.tail.split('/').mkString("") match {
  //     case x.path.r(_,_,_,_) => println("found!"); true //TODO this is not flexible 
  //     case _ => println("not found!",x.path.r ); println("hash", path.tail.split('/').mkString("")); false
  //   })
  // }
  
  private def dynamicRouteMatchFound(path: String) = {
    dynamicRoutes.exists( x => x.path.matchesUrl(path) )
  }
  
  private def getDynamicRoute(path: String) = {
    println("HEY")
    println(path.tail)
    println(dynamicRoutes.head.path.toString)
    //val pathLiteral = path.tail.split('/').mkString("")
    val matchingDynamicRoute = dynamicRoutes.find(x => x.path.matchesUrl(path)).getOrElse(Router.DynamicNotFound)
    
//    val params = matchingDynamicRoute match {
//      //TODO this is not seen as a seq of strings even if it is...
//      case Router.DynamicNotFound => println("this"); Router.DynamicNotFound.params.asInstanceOf[Seq[String]]
//      case _ => matchingDynamicRoute.path.getRouteParams(path.tail).map(_.toString)
//    }
    
    //  val params = if(matchingDynamicRoute.path != Router.DynamicNotFound.path){
    //    matchingDynamicRoute.path.getRouteParams(path.tail).map(_.toString)
    //  } else{
    //    Seq.empty[String]
    //  }
    
    //println("params", params)
    val view = matchingDynamicRoute.viewGenerator()
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
    
    // building only static routes, 
    // dynamic routes are built dynamically no neet to call build here
    //routes.foreach(x => log.warn("YES:", x.path))
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
        
    require(baseURL != null, "Base path required") //TODO set default to "/" if not specified
    require(!baseURL.isEmpty, "Base path required")
  }
  
  def navigateTo(path: String) = { 
    val newLocation = getRoute(path) 
    document.location.hash = path
    activePage.value = newLocation
  }
  
  def addRoute(route: RouteBuilder) = {
    routes.foreach(x => log.warn("rrrrrr", x.path))
    routes = routes :+ route
  }
  def addDynamicRoute(route: DynamicRouteBuilder) = dynamicRoutes = dynamicRoutes :+ route
  
  // private def setWindowLocation(path: String) = {
  //   val location = window.location
  //   location.hash_=(path)
  // }
  
  //TODO seems to be triggered many times....should this be a singleton???
  private val handleHashChange = (e: HashChangeEvent) => {
    val location = e.newURL
    val path = location.substring(location.indexOf("#")).toList match {
      case _ :: tail => tail.mkString("")
      case _ => s"#${Router.NotFound.path}" //TODO fix this: it's not doing anything
    }
    println("path is", path)
    //history.params = getParams(path)
    //println("PPPP", history.params)
    val route = getRoute(path)
    if (route == Router.NotFound.view || 
        route == Router.DynamicNotFound.viewGenerator()){
      //simulate redirect
      //document.location.hash = Router.NotFound.path
      e.stopImmediatePropagation()
    }
    activePage.value = route
  }
  
  window.addEventListener("hashchange", handleHashChange)
  //window.addEventListener("load", (e: Event) => history.params = getParams(window.location.hash.tail))
  //window.addEventListener("beforepageload", (e: Event) => {println("HI"); history.params = getParams(window.location.hash.tail)})
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
  //def getParams(path: String) = router.getParams(path)
  def getParams = params //TODO return immutable copy
}

//TODO add ability to set custom 404 page
private object Router {

  val NotFound = {
    val route = new RouteBuilder() 
    route.path = "/404"
    route.view = new RoutingView(){ @dom override val element = dummy.build.bind }
    route
  }
  
  val DynamicNotFound = {
    import DynamicRoute._
    val route = new DynamicRouteBuilder()
    route.path = "/404".toFragment
    route.viewGenerator = () => new RoutingView(){ @dom override val element = dummy.build.bind }
    route
  }
  
  def registerRoute(route: RouteBuilder, router: Router) = router.addRoute(route)
  
  def registerDynamicRoute(route: DynamicRouteBuilder, router: Router) = router.addDynamicRoute(route)
  
  def apply(baseURL: String) = new Router(baseURL)
}