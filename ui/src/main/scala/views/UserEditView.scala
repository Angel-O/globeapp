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
import app.Connect
//import app.UsersFetched

object UserEditView {
  
  val userEditView = new RoutingView() with Connect {
    
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
    //dispatch(FetchUsers)
    //connect to the circuit...car selector and user selector could be combined...
    val cars = Var(initialModel.cars.cars)
    val users = Var(initialModel.users.users)
    connect()(AppCircuit.carSelector, cars.value = AppCircuit.carSelector.value)
    connect()(AppCircuit.userSelector, users.value = AppCircuit.userSelector.value)
  }
  
  @dom def renderUsers(users: Seq[User]) = toBindingSeq(users).map(x => <li> { x.name } </li>)
  @dom def renderCars(cars: Seq[Car]) = toBindingSeq(cars).map(x => <li> { x.make } </li>)
}