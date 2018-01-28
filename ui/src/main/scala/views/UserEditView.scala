package views

import navigation.URIs._
import components.Components.Implicits._
import router.RoutingView
import apimodels.User
import app.{Rename, FetchUsers, FetchCars}
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.{Event, Node}
import com.thoughtworks.binding.Binding.BindingSeq
import diode.{Dispatcher, ModelRO, ActionHandler, ModelRW}
import app.{AppCircuit, AppModel, Car}

object UserEditView {
  
  import navigation.Navigators._
  val userEditView = new RoutingView() { 
    
    @dom override def element = {
      val view = new View(AppCircuit).render.bind
      view
    }
  }
  
  abstract class AbstractConnector[M, T](selector: ModelRW[M,T]) 
  extends { override val modelRW = selector } with ActionHandler[M,T](modelRW) {
    
    def handle = { case _ => noChange } 
    override def updated(newValue: T) = noChange
    override def updated(newValue: T, effect: diode.Effect) = noChange
    override def value = selector.value.asInstanceOf[T]
  }
  
  class SelectorConnector[M,T](selector: ModelRW[M, T]) 
  extends AbstractConnector(selector){
    override def value = selector.value.asInstanceOf[T]
  }
  
  abstract class Connect[M,T]
  extends AbstractConnector[M, T](AppCircuit.defaultSelector[M,T]) {    
    import org.scalajs.dom.document
    def render: Binding[Node] 
    def mount = dom.render(document.body, render.asInstanceOf[Binding[Node]]) 
    AppCircuit.subscribe(AppCircuit.zoom(identity)) (_ => mount)
  }
  
  //TODO make app circuit and dispatch implicit
  class View[M,T](dispatch: Dispatcher) extends Connect[M, T]{
  
    val userConnector = new SelectorConnector(AppCircuit.userSelector)
    val carConnector = new SelectorConnector(AppCircuit.carSelector)
    
    @dom def renderUsers(users: Seq[User]) = {
      toBindingSeq(users).map(x => <li> {x.name} </li>)
    }
    
    @dom def renderCars(cars: Seq[Car]) = {
      toBindingSeq(cars).map(x => <li> {x.make} </li>)
    }
  
    @dom def render = {
      
      <div>
				<ul>{renderUsers(userConnector.value).bind}</ul>
      	<button 
					onclick={(e: Event) => dispatch(Rename(1, "Dom"))}> Rename user </button>
				<button 
					onclick={(e: Event) => dispatch(FetchUsers)}> Get users </button>
				<ul>{renderCars(carConnector.value).bind}</ul>
				<button 
					onclick={(e: Event) => dispatch(FetchCars)}> Get cars </button>
			</div>
    }
  }
}