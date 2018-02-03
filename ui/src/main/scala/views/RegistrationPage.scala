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

object RegistrationPage {
  import navigation.Navigators._
  val page = new RoutingView() {
     
    @dom override def element = view.element.bind
    
    val view = new ConnectedView(AppCircuit, AppCircuit.zoom(am => am.users.users)){
      @dom override def element = {
      
        <ModalCard 
					label={"Launch form"} 
					title={"Register now"} 
					content={<div>
										<RegistrationForm 
											onSubmit={navigateToHello _} 
											onClick={navigateToUserEdit _} 
											fetchUsers={fetchUsers _}/>
									</div>}
					onSave={navigateToHome _}/>.build.bind
        
      }
      def fetchUsers() = dispatch(FetchUsers) //TODO this needs to be awaited...       
    }
    
    //val users = Var(AppCircuit.initialModel.users.users)
    //val userConnector = new SelectorConnector(AppCircuit.userSelector, AppCircuit.zoom(am => am.users.users), users.value = AppCircuit.userSelector.value)
  }
  //val users = new SelectorConnector(AppCircuit.userSelector).value
}