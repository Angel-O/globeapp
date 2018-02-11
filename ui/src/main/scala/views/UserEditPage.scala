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
    
    //route params are lazily evaluated (use lazy val inside RoutingView)
    //lazy val name = routeParams(0) 
    //lazy val posts = routeParams(1) 
    
    @dom override def element = {
         //TODO get by name rather than index
         // (use val inside RoutingView.element)
         val name = routeParams(0) 
         val posts = routeParams(1) 
         
          <div> 
						<h1> {name} - You have {posts} posts</h1>
						<ul>{ renderUsers(users.bind).bind }</ul>           
						<br/>
            <Button label="Rename user" onClick={ () => dispatch(Rename(1, "Dom")) }/>
            <Button label="Get users" onClick={ () => dispatch(FetchUsers) }/>
            <ul>{ renderCars(cars.bind).bind }</ul>
						<br/>
            <Button label="Get cars" onClick={ () => dispatch(FetchCars) }/>
						<Button label="Form" onClick={ navigateToForm _ }/>
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