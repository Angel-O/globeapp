package appstate

import diode.Action

//TODO remove this...it is just for testing

// Model 
case class Cars(cars: Seq[Car])
case object Cars { def apply() = new Cars(Seq()) }

// Actions
case class Car(make: String)
case object FetchCars extends Action