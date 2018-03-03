package views

import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import org.scalajs.dom.{document, Node}
import components.Components.Implicits.CustomTags2
import components.Components.Implicits._
import navigation.URIs._
import config._
import appstate.{AuthSelector, VerifyToken, Logout, Connect, FetchAllMobileApps}
import utils.Push

//case class Props(username: String, loggedIn: Boolean)

object App extends AuthSelector with Push {

  // keeping track of logged in status (children components are affected by it)
  val loggedIn: Var[Boolean] = Var(getLoggedIn())
  val username: Var[String] = Var(getUsername())

  def main(args: Array[String]): Unit = {

    val routes = RouteProvider.routes

    @dom def render = {

      //TODO pass config object to router
      MainShell
        .render(
          <div><BrowserRouter baseUrl={HomePageURI} routes={routes.bind}/></div>,
          loggedIn.bind,
          username.bind,
          navigateToLogin _,
          doLogout _,
          navigateToCatalog _)
        .bind
    }

    // handling redirection on page refresh
    // TODO check for validity as well ...aka token expiration
    // this line can be moved after mounting the app to see the
    // init location logged on the console
    dispatch(VerifyToken)

    // mount the App
    dom.render(document.body, render.asInstanceOf[Binding[Node]])
  }

  def doLogout() = dispatch(Logout)
  def navigateToLogin() = push(LoginPageURI)
  def navigateToCatalog() = {
    //dispatch(FetchAllMobileApps)
    push(CatalogPageURI)
    }

  def connectWith = {
    loggedIn.value = getLoggedIn()
    username.value = getUsername()
  }
}
