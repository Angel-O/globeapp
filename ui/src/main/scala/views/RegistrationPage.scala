package views

import router.RoutingView
import com.thoughtworks.binding.dom
import components.Components.Implicits.{CustomTags2, _}
import hoc.form._
import app.AppCircuit
import app.AppModel
import app.FetchUsers
import apimodels.User
import diode.Dispatcher
import com.thoughtworks.binding.Binding.Var
import app.Connect

object RegistrationPage {
  import navigation.Navigators._
  val page = new RoutingView() with Connect{
     
    var users = initialModel.users.users
    @dom override def element = {
      <ModalCard 
					label={"Launch form"} 
					title={"Register now"} 
					content={<div>
										<RegistrationForm 
											onSubmit={navigateToHello _} 
											onClick={navigateToUserEdit _} 
											fetchUsers={fetchUsers _}
											users={users}/>
									</div>}
					onSave={navigateToHome _}/>.build.bind
    }
    
    def fetchUsers() = dispatch(FetchUsers)
  }
}