package views

import com.thoughtworks.binding.{dom, Binding}
import org.scalajs.dom.{document, Node}
import components.Components.Implicits.CustomTags2
import components.Components.Implicits._
import navigation.URIs._
//import org.scalajs.dom.window
//import utils.Push
import config._
import appstate.{AuthSelector, VerifyToken}
import appstate.Connect

object App extends Connect { //{AuthSelector {

  def main(args: Array[String]): Unit = {

    val routes = RouteProvider.routes

    @dom def render = {

      // build the router (this could just be wrapped into a div,
      // to handle building and binding automatically rather than
      // calling them manually)
      //TODO pass config object
      MainShell
        .render(
          <div><BrowserRouter baseUrl={HomePageURI} routes={routes.bind}/></div>)
        .bind
    }

    // handling redirection on page refresh
    // TODO check for validity as well ...aka token expiration
    // this line can be moved after mounting the app to see the
    // init location logged on the console
    // if (window.sessionStorage.getItem(AUTHORIZATION_HEADER_NAME) == null) {
    //   push(LoginPageURI)
    // }

    dispatch(VerifyToken)

    // mount the App
    dom.render(document.body, render.asInstanceOf[Binding[Node]])
  }

  //def connectWith() = ()
}
