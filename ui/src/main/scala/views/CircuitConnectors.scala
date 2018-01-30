package views

import diode.ModelRW
import diode.ActionHandler
import com.thoughtworks.binding.Binding
import app.AppCircuit
import org.scalajs.dom.raw.Node
import com.thoughtworks.binding.dom
import diode.Dispatcher
import org.scalajs.dom.raw.HTMLElement
import diode.Circuit

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
    import scala.language.implicitConversions
    implicit def selectorConnectorWrapper[M,T](rw: ModelRW[M,T]) = new SelectorConnector(rw)
    def element: Binding[Node] 
    def mount = dom.render(document.body, element.asInstanceOf[Binding[Node]]) 
    AppCircuit.subscribe(AppCircuit.zoom(identity)) (_ => mount)
  }
  
  class ConnectedView[M <: AnyRef,T](circuit: Circuit[M]) extends Connect[M, T]{
    def element: Binding[HTMLElement] = throw new Exception("Element of connected view must be overriden")
    implicit var dispatch: Dispatcher = circuit
  }