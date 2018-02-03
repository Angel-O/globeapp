package views

import app.{AppCircuit}
import router.RoutingView
import com.thoughtworks.binding.dom
import apimodels.User
import app.Car
import org.scalajs.dom.raw.Event
import app.{Rename, FetchUsers, FetchCars}
import navigation.Navigators._
import components.Components.Implicits._
import diode.Dispatcher
import com.thoughtworks.binding.Binding.Var

object UserEditView {
  
  val userEditView = new RoutingView() {
    
    @dom override def element = view.element.bind
   
    val view = new ConnectedView(AppCircuit) {
        
        @dom override def element = {
      
          <div> 
						<ul>{ renderUsers(users.bind).bind }</ul>           
						<br/>
            <button onclick={ (e: Event) => dispatch(Rename(1, "Dom")) }> Rename user </button>
            <button onclick={ (e: Event) => dispatch(FetchUsers) }> Get users </button>
            <ul>{ renderCars(cars.bind).bind }</ul>
						<br/>
            <button onclick={ (e: Event) => dispatch(FetchCars) }> Get cars </button>
						<button onclick={ (e: Event) => navigateToForm() }> Form </button>
						<br/>           
          </div>
          
        }
        
        //connect to the circuit...car selector and user selector could be combined...
        //api can be improved...
        val cars = Var(AppCircuit.initialModel.cars.cars)
        val users = Var(AppCircuit.initialModel.users.users)
        val carConnector = new SelectorConnector(AppCircuit.carSelector, AppCircuit.zoom(am => am.cars.cars), cars.value = AppCircuit.carSelector.value)
        val userConnector = new SelectorConnector(AppCircuit.userSelector, AppCircuit.zoom(am => am.users.users), users.value = AppCircuit.userSelector.value)
    }
  }
  
  //val carConnector = new SelectorConnector(AppCircuit.carSelector)
  //val userConnector = new SelectorConnector(AppCircuit.userSelector, null) 
  @dom def renderUsers(users: Seq[User]) = toBindingSeq(users).map(x => <li> { x.name } </li>)
  @dom def renderCars(cars: Seq[Car]) = toBindingSeq(cars).map(x => <li> { x.make } </li>)
}