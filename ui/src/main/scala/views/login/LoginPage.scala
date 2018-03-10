package views.login

import components.core.Implicits._
import components.Components.CustomTags2
import com.thoughtworks.binding.dom
import router.RoutingView
import hoc.form.LoginForm
import appstate.{Login, Connect}
import appstate.AppCircuit._
import org.scalajs.dom.raw.Event
import navigation.Navigators._

//TODO extract custom styling
//TODO make routing view a trait
object LoginPage {
  def view() = new RoutingView() {

    @dom
    override def element = {
      <div>
        <SimpleModal 
          isPageModal={true}
          onSmartClose={onSmartClose _}
          content={
            <div style={"display: flex; flex-direction: column"}>
                <LoginForm onSubmit={ handleSubmit _ }/>
                <br/>
                <p style={"color: #00cc99"}> Don't have an account? 
                  <span 
                    style="text-decoration: underline; cursor: pointer" 
                    onclick={(_: Event) => navigateToRegister()}>
                    Register
                  </span>
                </p>
            </div>}/>
      </div>
    }

    def handleSubmit(username: String, password: String) =
      dispatch(Login(username, password))

    def onSmartClose() = navigateTo(history.getLastVisited)
  }
}
