package appstate;
//package views

//import diode.ModelRW
//import diode.ActionHandler
//import com.thoughtworks.binding.Binding
//import app.AppCircuit
//import org.scalajs.dom.raw.Node
//import com.thoughtworks.binding.dom
//import diode.Dispatcher
//import org.scalajs.dom.raw.HTMLElement
//import diode.Circuit
//import diode.ModelR
//import diode.Circuit
//
//import components.Components.Implicits.log
//import scalajs.js
//import org.scalajs.dom.raw.HTMLElement
//import org.scalajs.dom.raw.ParentNode
//import app.AppModel

//abstract class AbstractConnector[M, T](selector: ModelRW[M,T]) 
//  extends { override val modelRW = selector } with ActionHandler[M,T](modelRW) {
//    
//    def handle = { case _ => noChange } 
//    override def updated(newValue: T) = noChange
//    override def updated(newValue: T, effect: diode.Effect) = noChange
//    override def value = selector.value.asInstanceOf[T]
//}
  
//class Connect[M <: AnyRef,T](
//    //selector: ModelRW[M, T], 
//    cursor: ModelR[M,T] = AppCircuit.zoom(identity), 
//    update: => Unit = Unit){ 
////extends AbstractConnector(selector){
//  def value = cursor.value.asInstanceOf[T]
//  
//  private val ac: Circuit[M] = AppCircuit.asInstanceOf[Circuit[M]]
//  ac.subscribe(cursor) (_ => update)
//}
  
  //TODO make more reusable by not using AppCircuit...
//  abstract class Connect[M <: AnyRef,T](cursor: ModelR[M,T] = AppCircuit.zoom(identity))
//  extends AbstractConnector[M, T](AppCircuit.defaultSelector[M,T]) {    
    //import org.scalajs.dom.document
    //import scala.language.implicitConversions
    //implicit def selectorConnectorWrapper[M <: AnyRef,T](rw: ModelRW[M ,T]) = new SelectorConnector(rw)
    //def element: Binding[Node] 
    //def mount = {}//dom.render(document.body, element.asInstanceOf[Binding[Node]])  //{
      //val container = Binding{document.getElementById("pp2").asInstanceOf[HTMLElement]}
      //val parent = element.bind.parentNode.asInstanceOf[HTMLElement]
      
      //cursor.eval(AppModel.asInstanceOf[M])
    //}
    //private val ac: Circuit[M] = AppCircuit.asInstanceOf[Circuit[M]]
    
    //ac.subscribe(cursor) (_ => mount)
 // }
  
//  class ConnectedView[M <: AnyRef,T](circuit: Circuit[M], cursor: ModelR[M,T] = AppCircuit.zoom(identity)) 
//  extends Connect[M, T](cursor){
//    def element: Binding[HTMLElement] = throw new Exception("Element of connected view must be overriden")
//    implicit val dispatch: Dispatcher = circuit
//  }
  
//  abstract class CircuitView[M <: AnyRef,T] 
//  extends{val cursor: ModelR[M,T] = AppCircuit.zoom(identity).asInstanceOf[ModelR[M,T]]} 
//  with Connect[M, T](cursor){
//    val connected: Connected[M, T]
//  }
  
//  class Connected[M <: AnyRef,T](circuit: Circuit[M], cursor: ModelR[M,T] = AppCircuit.zoom(identity)) 
//  extends Connect[M,T](cursor){
//    private var storeViewInner: T = cursor.value
//    def element = () => storeViewInner = cursor.value
//    def storeView = storeViewInner
//    implicit val dispatch: Dispatcher = circuit
//  }