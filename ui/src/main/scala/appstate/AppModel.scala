package appstate

import apimodels.User
import diode.Action
import com.thoughtworks.binding.Binding.BindingSeq
import diode.data.Pot
import diode.data.PotAction
import diode.data.Empty

case class AppModel(users: Users, cars: Cars, self: AppModel = null) 

case class Users(users: Seq[User])
case object Users{
  def apply() = new Users(Seq())
}

case class Rename(id: String, name: String) extends Action

case class ChangeId(oldId: String, newId: String) extends Action

case class CreateUser(name: String) extends Action

case object FetchUsers extends Action

case class UsersFetched(potResult: Pot[Seq[User]] = Empty) extends PotAction[Seq[User], UsersFetched]{
  def next(newResult: Pot[Seq[User]]) = UsersFetched(newResult)
}

case class DeleteUser(id: String) extends Action


//TESTING...
case class Car(make: String)

case object FetchCars extends Action

case class Cars(cars: Seq[Car])
case object Cars {
  def apply() = new Cars(Seq())
}