package views

import appstate.Connect
import appstate.CreateUser
import components.Components.Implicits._
import router.RoutingView
import com.thoughtworks.binding.dom
import hoc.form._
import appstate.{Login, AppCircuit}
import org.scalajs.dom.raw.Event
import com.thoughtworks.binding.Binding.Var
import diode.NoAction
import diode.ActionBatch

//TODO extract custom styling
//TODO make routing view a trait
object LoginPage {
  import navigation.Navigators._
  def view() = new RoutingView() with Connect {

    @dom
    override def element = {
      <SimpleModal openAtLaunch={true} smartClose={false}
				content={
          <div style={"display: flex; flex-direction: column"}>
      				<LoginForm onSubmit={ handleSubmit _ }/>
              <br/>
              <p style={"color: #00cc99"}>Don't have an account? 
                <span 
                  style={"text-decoration: underline;  cursor: pointer"} 
                  onclick={(e: Event) => navigateToRegister()}>Register</span>
              </p>
        	 </div>}
			/>.build.bind
    }

    def handleSubmit(username: String, password: String) = {
      dispatch(Login(username, password, navigateToHome _))
    }

    // def forwardToHomePage(errorCodeOption: Option[Int]) = {
    //   //if there is no error code proceed to the home page otherwise do nothing
    //   errorCodeOption.fold(navigateToHome())(_ => Unit)
    // }

    // //TODO will this work when log out is implemented ??
    // connect()(AppCircuit.authSelector,
    //           forwardToHomePage(AppCircuit.authSelector.value.errorCode))
  }
}
