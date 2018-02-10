package views

import appstate.{AppCircuit}
import router.RoutingView
import com.thoughtworks.binding.dom
import apimodels.User
import appstate.Car
import org.scalajs.dom.raw.Event
import appstate.{Rename, FetchUsers, FetchCars}
import navigation.Navigators._
import components.Components.Implicits._
import diode.Dispatcher
import com.thoughtworks.binding.Binding.Var
import appstate.Connect
//import appstate.UsersFetched

object UserEditPage {
  
  def view() = new RoutingView() with Connect {
    
    //route params are lazily evaluated
    //TODO get by name rather than index
    lazy val name = routeParams(0) 
    lazy val posts = routeParams(1) 
    
    @dom override def element = {
      
          <div> 
						<h1> {name} - You have {posts.toString} posts</h1>
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