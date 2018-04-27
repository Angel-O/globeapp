package views

import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import org.scalajs.dom.{document, Node}
import components.core.Implicits._
import components.Components.Router
import navigation.URIs._
import config._
import appstate.{VerifyToken, Logout, Connect, FetchAllMobileApps}
import appstate.AppCircuit._
import appstate.AuthSelector._
import utils.Push
import router.Config

//case class Props(username: String, loggedIn: Boolean)

object App extends Push {
  // keeping track of logged in status (children components are affected by it)
  val loggedIn: Var[Boolean] = Var(getLoggedIn())
  val username: Var[String] = Var(getUsername())

  def main(args: Array[String]): Unit = {

    @dom def render = {
      val config = Config(baseUrl = HomePageURI,
                          routes = RouteProvider.routes.bind,
                          notFoundUrl = NotFoundPageURI)

      MainShell
        .render(
          <div><BrowserRouter config={config}/></div>,
          loggedIn.bind,
          username.bind,
          navigateToLogin _,
          doLogout _,
          navigateToCatalog _,
          navigateToPolls _,
          navigateToFavoriteApps _,
          navigate _
        )
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
  def navigateToCatalog() = push(CatalogPageURI)
  def navigateToPolls() = push(PollsPageURI)
  def navigateToFavoriteApps() = push(FavoriteAppsPageURI)
  def navigate() = push(s"$CatalogPageURI/55")

  def update = {
    loggedIn.value = getLoggedIn()
    username.value = getUsername()
  }

  connect(update)(authSelector)
}
