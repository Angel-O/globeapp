package views.error

import components.core.Implicits._
import components.Components.Modal
import com.thoughtworks.binding.dom
import router.RoutingView
import hoc.form.LoginForm
import appstate.{Login, Connect}
import appstate.AppCircuit._
import org.scalajs.dom.raw.Event
import navigation.Navigators._
import config._

object ErrorPage {
  def unavailable() = new RoutingView() {

    @dom
    override def element = {
      <div style={"display: flex; align-items: center; justify-content: space-around"}>
        <h1>ERROR OCCURRED...We are working to solve the issue: try later <a href={s"#$ROOT_PATH"}>home</a></h1>
      </div>
    }
  }

  def notFound() = new RoutingView() {

    @dom
    override def element = {
      <div style={"display: flex; align-items: center; justify-content: space-around"}>
        <h1>PAGE NOT FOUND <a href={s"#$ROOT_PATH"}>home</a></h1>
      </div>
    }
  }
}
