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
import diode.Action
import utils.log
import scalajs.js
import upickle.default.{ReadWriter => RW, macroRW}
  
import components.Components.Implicits.ComponentBuilder


// Represents the portion of the state that will be serialized 
// in the location storage to be retrieved after a browser refresh
case class PersistentState(username: String)
case object PersistentState{
  def apply(username: String) = new PersistentState(username)
  def apply() = new PersistentState("") //TODO use option after finding out how to persist global state
  implicit def rw: RW[PersistentState] = macroRW
}

// Global state tree
case class AppModel(users: Users, cars: Cars, auth: Auth, self: AppModel = null)

object AppCircuit extends Circuit[AppModel] with ModelLens[AppModel] {

  def initialModel = AppModel(Users(), Cars(), Auth())

  def currentModel = zoom(identity).value

  //NO longer used...
  def defaultSelector[M, T]: ModelRW[M, T] =
    zoomTo(x => x.self).asInstanceOf[ModelRW[M, T]]
  
  val userSelector = zoomTo(x => x.users.users)
  val carSelector = zoomTo(x => x.cars.cars)
  val authSelector = zoomTo(x => x.auth.params)

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
trait ModelLens[M <: AnyRef] {
  def initialModel: M
  def currentModel: M
}
abstract class Connector[M <: AnyRef](circuit: Circuit[M] with ModelLens[M])
    extends ComponentBuilder
    with ModelLens[M] {

  //val dispatch: Dispatcher = circuit
  def dispatch(action: Action) = {
    log.warn("ACTION", action.toString)
    log.warn("STATE-BEFORE: ", circuit.currentModel.asInstanceOf[js.Any]);
    circuit.apply(action)
  }

  def initialModel = circuit.initialModel
  def currentModel = circuit.currentModel

  def connect[M <: AnyRef, T]()(cursor: ModelR[M, T] = circuit.zoom(identity),
                                update: => Unit = Unit) {

    val ac: Circuit[M] = circuit.asInstanceOf[Circuit[M]]
    def loggedUpdate() = {
      update; log.warn("STATE-AFTER", currentModel.asInstanceOf[js.Any])
    }
    ac.subscribe(cursor)(_ => loggedUpdate())
  }
}

abstract class ConnectorBuilder extends Connector[AppModel](AppCircuit)

//Use generic connect rather than Connect since generic connect 
//is decoupled from AppCircuit and use only ConnectorBuilder or Connector
trait Connect {
  def dispatch(action: Action) = {
    log.warn("ACTION", action.toString)
    log.warn("STATE-BEFORE: ", AppCircuit.currentModel.asInstanceOf[js.Any]);
    AppCircuit.apply(action)
  }
  def initialModel = AppCircuit.initialModel
  def value = AppCircuit.currentModel

  //TODO investigate: equal sign is missing before method body and
  // double set of params is weird...
  def connect[M <: AnyRef, T]()(
      cursor: ModelR[M, T] = AppCircuit.zoom(identity),
      update: => Unit = Unit) {

    val ac: Circuit[M] = AppCircuit.asInstanceOf[Circuit[M]]
    def loggedUpdate() = {
      update
      log.warn("STATE-AFTER: ", AppCircuit.currentModel.asInstanceOf[js.Any])
    }
    ac.subscribe(cursor)(_ => loggedUpdate())
  }
}

trait GenericConnect[M <: AnyRef, T] extends ConnectWith {
  def dispatch(action: Action) = {
    log.warn("ACTION", action.toString)
    log.warn("STATE-BEFORE: ", circuit.currentModel.asInstanceOf[js.Any]);
    circuit.apply(action)
  }
  
  val cursor: ModelR[M, T]
  val circuit: Circuit[M] with ModelLens[M]

  def value = cursor.value
  def initialModel = circuit.initialModel
  
  def connectWith(): Unit

  def connect() = { 
    def loggedUpdate() = {
      connectWith()
      log.warn("STATE-AFTER: ", circuit.currentModel.asInstanceOf[js.Any])
    }

    circuit.subscribe(cursor)(_ => loggedUpdate())
  }
}

// Note: defining this method separately because the compiler complains about empty names
trait ConnectWith{
  def connectWith(): Unit
}

trait SilentConnect[M <: AnyRef,T] extends GenericConnect[M,T]{
  def connectWith() = Unit
}
