package appstate

import diode.Circuit
import diode.ActionHandler
import diode.ModelRW
import apimodels.User
import ApiCalls._
import diode.Dispatcher
import diode.ModelR
import diode.data.PotState._
import diode.data.Pot
import diode.Effect

import components.Components.Implicits.ComponentBuilder

// Global state tree
case class AppModel(users: Users, cars: Cars, auth: Auth, self: AppModel = null) 

object AppCircuit extends Circuit[AppModel] with InitialModel[AppModel] {
  
  def initialModel = AppModel(Users(), Cars(), Auth())
  
  //NO longer used...
  def defaultSelector[M,T]: ModelRW[M,T] = zoomTo(x => x.self).asInstanceOf[ModelRW[M,T]]
  val userSelector = zoomTo(x => x.users.users)
  val carSelector = zoomTo(x => x.cars.cars)
  val authSelector = zoomTo(x => x.auth.jwt)
  
  // Using foldHandlers rather than composeHandlers to
  // allow all handlers to process the actions without stopping
  // soon as the the action has been handled
  override val actionHandler = foldHandlers(
    new UserHandler(userSelector),
    new CarHandler(carSelector),
    new AuthHandler(authSelector)
  )
}





// TODO move this to separate project...ala Redux
trait InitialModel[M <: AnyRef] {
  def initialModel: M
}
abstract class Connector[M <: AnyRef](circuit: Circuit[M] with InitialModel[M]) 
extends ComponentBuilder with InitialModel[M]{
  
  val dispatch: Dispatcher = circuit
  def initialModel = circuit.initialModel
  
  def connect[M <: AnyRef,T]()(
    cursor: ModelR[M,T] = circuit.zoom(identity), 
    update: => Unit = Unit){
    
    val ac: Circuit[M] = circuit.asInstanceOf[Circuit[M]]
    ac.subscribe(cursor) (_ => update)
  }
}

abstract class ConnectorBuilder extends Connector[AppModel](AppCircuit)

//TODO make this decoupled from AppCircuit and use only ConnectorBuilder or Connector
trait Connect{
  val dispatch: Dispatcher = AppCircuit
  def initialModel = AppCircuit.initialModel
  
  def connect[M <: AnyRef,T]()(
    cursor: ModelR[M,T] = AppCircuit.zoom(identity), 
    update: => Unit = Unit){
    
    val ac: Circuit[M] = AppCircuit.asInstanceOf[Circuit[M]]
    ac.subscribe(cursor) (_ => update)
 }
}