package appstate

import diode.Action
import apimodels.User
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction

// Model
case object Users{ def apply() = new Users(Seq()) }
case class Users(users: Seq[User])


// Actions
case class Rename(id: String, name: String) extends Action
case class ChangeId(oldId: String, newId: String) extends Action
case class CreateUser(name: String) extends Action
case object FetchUsers extends Action
case class DeleteUser(id: String) extends Action
case class UsersFetched(potResult: Pot[Seq[User]] = Empty) extends PotAction[Seq[User], UsersFetched]{
  def next(newResult: Pot[Seq[User]]) = UsersFetched(newResult)
}