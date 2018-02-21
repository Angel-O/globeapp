package views

import appstate.Connect
import appstate.CreateUser
import components.Components.Implicits._
import router.RoutingView
import com.thoughtworks.binding.dom
import hoc.form._

object LoginPage {
  import navigation.Navigators._
  def view() = new RoutingView() with Connect {
    
    @dom
    override def element = {
      <SimpleModal openAtLaunch={true} smartClose={false}
				content={
          <div>
      				<LoginForm onSubmit={ () => println("What") }/>
        	 </div>}
			/>.build.bind
    }

    def handleSubmit(name: Any) = {
      dispatch(CreateUser(name.toStr))
      navigateToUserEdit()
    }
  }
}
