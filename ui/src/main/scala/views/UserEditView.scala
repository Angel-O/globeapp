package views

import app.{AppCircuit, AppModel}
import router.RoutingView
import com.thoughtworks.binding.dom
import apimodels.User
import app.Car
import org.scalajs.dom.raw.Event
import app.{Rename, FetchUsers, FetchCars}
import navigation.Navigators._
import components.Components.Implicits._

object UserEditView {
  
  val userEditView = new RoutingView() {

    //needs to be returned explicitly or it maay cause the @dom macro to fail
    @dom override def element = view.element.bind
     
    val view = new ConnectedView(AppCircuit) {
        
        @dom override def element = {
          <div>
            <ul>{ renderUsers(userConnector.value).bind }</ul>
            <button onclick={ (e: Event) => dispatch(Rename(1, "Dom")) }> Rename user </button>
            <button onclick={ (e: Event) => dispatch(FetchUsers) }> Get users </button>
            <ul>{ renderCars(carConnector.value).bind }</ul>
            <button onclick={ (e: Event) => dispatch(FetchCars) }> Get cars </button>
            <button onclick={ (e: Event) => navigateToForm() }> Go to form </button>
          </div>
        }
    }
  }
  //connect ot the circuit
  val carConnector = new SelectorConnector(AppCircuit.carSelector)
  val userConnector = new SelectorConnector(AppCircuit.userSelector)   
  @dom def renderUsers(users: Seq[User]) = toBindingSeq(users).map(x => <li> { x.name } </li>)
  @dom def renderCars(cars: Seq[Car]) = toBindingSeq(cars).map(x => <li> { x.make } </li>)
}