package app

import apimodels.User
import diode.Action
import com.thoughtworks.binding.Binding.BindingSeq
import diode.data.Pot

case class AppModel(users: Users, cars: Cars, self: AppModel = null) 

case class Users(users: Seq[User])
case object Users{
  def apply() = new Users(Seq())
}

case class Rename(id: Int, name: String) extends Action

case class ChangeId(oldId: Int, newId: Int) extends Action

case object FetchUsers extends Action

case class UsersFetched(users: Pot[Seq[User]]) extends Action


//TESTING...
case class Car(make: String)

case object FetchCars extends Action

case class Cars(cars: Seq[Car])
case object Cars {
  def apply() = new Cars(Seq())
}