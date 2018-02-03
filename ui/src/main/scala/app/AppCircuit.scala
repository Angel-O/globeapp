package app

import diode.Circuit
import diode.ActionHandler
import diode.ModelRW
import apimodels.User
import ApiCalls._
import diode.Dispatcher
import diode.ModelR

object AppCircuit extends Circuit[AppModel] {
  
  def initialModel = AppModel(Users(), Cars())
  
  //NO longer used...
  def defaultSelector[M,T]: ModelRW[M,T] = zoomTo(x => x.self).asInstanceOf[ModelRW[M,T]]
  val userSelector = zoomTo(x => x.users.users)
  val carSelector = zoomTo(x => x.cars.cars)
  
  // Using foldHandlers rather than composeHandlers to
  // allow all handlers to process the actions without stopping
  // soon as the the action has been handled
  override val actionHandler = foldHandlers(
    new UserHandler(userSelector),
    new CarHandler(carSelector)
  )
}

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

class CarHandler[M](modelRW: ModelRW[M, Seq[Car]]) extends ActionHandler(modelRW){
  override def handle = {
    case FetchCars => {
      val cars = Seq(Car("Rari"), Car("Lambo"))
      if (modelRW.value != cars) updated(cars) else noChange
    }
  }
}

class UserHandler[M](modelRW: ModelRW[M, Seq[User]]) extends ActionHandler(modelRW){
  override def handle = {
    case Rename(id, name) => {
      val toRename = getUserById(id)
      val renamed = User(name, toRename.id)
      updated(value.map(x => if(x.id != id) x else renamed))
    }   
    case ChangeId(oldId, newId) => {
      updated(value.map(x => x.id == oldId match {
        case true => User(getUserById(oldId).name, newId)
        case _ => x
        }))
    }   
    case FetchUsers => effectOnly(fetchUsersEffect())
    case UsersFetched(users) => {
      users.state match { //TODO handle different states as well if necessary
        case isReady => updated(users.get)
        case isFailed => noChange //TODO log errors, but not here...
        case isPending => noChange //not triggered atm
        case _ => noChange
      }
    }
  }

  private def getUserById(id: Int) = value.find(_.id == id).get
}