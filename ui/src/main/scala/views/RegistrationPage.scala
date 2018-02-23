package views

import router.RoutingView
import com.thoughtworks.binding.dom
import components.Components.Implicits.{CustomTags2, _}
import hoc.form._
import appstate.AppCircuit
import appstate.AppModel
import appstate.FetchUsers
import apimodels.User
import diode.Dispatcher
import com.thoughtworks.binding.Binding.Var
import appstate.Connect
import appstate.UsersFetched
import diode.data.Empty
import appstate.FetchCars
import appstate.CreateUser
import appstate.Register

object RegistrationPage {
  import navigation.Navigators._
  def view() = new RoutingView() with Connect { //TODO connect is not needed

    @dom override def element = {
      <div>
        <SimpleModal openAtLaunch={true} smartClose={false} content=
          {<div>
            <RegistrationForm 
              onSubmit={handleSubmit _}  
              fetchUsers={fetchUsers _}/>
          </div>}/>
      </div>
    }

    def fetchUsers() = dispatch(FetchUsers)

    def handleSubmit(name: String,
                     username: String,
                     email: String,
                     password: String,
                     gender: String) = {
      dispatch(Register(name, username, email, password, gender))
    }
  }
}
