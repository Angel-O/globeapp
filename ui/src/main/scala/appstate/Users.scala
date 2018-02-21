package appstate

import diode.Action
import apimodels.User
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction
import diode.ModelRW
import diode.ActionHandler
import diode.data.PotState._
import ApiCalls._

//TODO most of the stuff here is no longer needed (on hold for now unless admin panel is implemented), users are created
// upon registration, api users are sent via jwt...
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

// Action Handler
class UserHandler[M](modelRW: ModelRW[M, Seq[User]]) extends ActionHandler(modelRW){
  override def handle = {
    case Rename(id, name) => {
      val toRename = getUserById(id)
      val renamed = User(toRename.id, name)
      updated(value.map(x => if(x.id != id) x else renamed), updateUserEffect(id, renamed))
    }   
    case ChangeId(oldId, newId) => {
      updated(value.map(x => x.id == oldId match {
        case true => User(newId, getUserById(oldId).username)
        case _ => x
        }))
    } 
    case CreateUser(name) => {
      //Effect.action()
      val user = User("dummy id", name)
      updated(value :+ user, createUserEffect(user)) //TODO add effect...
    }
    case DeleteUser(id) => {
      updated(value.filter(_.id != id), deleteUserEffect(id))
    }
    //TODO fix this...use pot actions like they should be used...
    case FetchUsers => effectOnly(fetchUsersEffect())
    case action @ UsersFetched(users) => {
      action handle { // equivalent to users.state match ===> handles the state of the action
        case PotEmpty => {
          println("nothing yet")
          updated(action.potResult.pending().get)
        }
        case PotReady => {
          println("data is here")
          //println("THERE", users.get)
          updated(action.potResult.ready(users.get).get)
        }
        case PotFailed => {
          val ex = action.result.failed.get
          updated(action.potResult.fail(ex).get)
          //println(users); 
          //noChange //TODO log errors, but not here...
        }
        case PotPending => {
          if(action.potResult.isPending){
            println("on its way...");
            updated(action.potResult.pending().get)//not triggered atm
          }
          println("nothing changed...");
          noChange //not triggered atm
        }
        case _ => noChange
      }
    }
  }

  private def getUserById(id: String) = value.find(_.id == id).get
}