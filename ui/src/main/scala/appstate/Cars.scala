package appstate

import diode.Action
import diode.ModelRW
import diode.ActionHandler

//TODO remove this...it is just for testing

// Model 
case class Cars(cars: Seq[Car])
case object Cars { def apply() = new Cars(Seq()) }

// Actions
case class Car(make: String)
case object FetchCars extends Action

// Action handler
class CarHandler[M](modelRW: ModelRW[M, Seq[Car]]) extends ActionHandler(modelRW){
  override def handle = {
    case FetchCars => {
      val cars = Seq(Car("Rari"), Car("Lambo"))
      if (modelRW.value != cars) updated(cars) else noChange
    }
  }
}