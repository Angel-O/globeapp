package appstate

import diode.Action
import apimodels.user.User
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction
import diode.ModelRW
import diode.ActionHandler
import diode.data.PotState._
import UserEffects._
import config._
import diode.Effect

// Model
case object Users { def apply() = new Users(Seq()) }
case class Users(users: Seq[User])

// Primary Actions
case object FetchUsers extends Action

// Derived Actions
case class UsersFetched(users: Seq[User]) extends Action
case class UsersFetchedFailed(message: String) extends Action

// Action Handler
class UserHandler[M](modelRW: ModelRW[M, Seq[User]])
    extends ActionHandler(modelRW) {
  override def handle = {
    case FetchUsers                  => effectOnly(fetchUsersEffect())
    case UsersFetched(users)         => updated(users)
    case UsersFetchedFailed(message) => noChange
  }
}

// Effects
object UserEffects {
  import scala.concurrent.ExecutionContext.Implicits.global
  import utils.api._

  def fetchUsersEffect() = {
    Effect(
      Get(url = s"$AUTH_SERVER_ROOT/api/users")
        .map(xhr => UsersFetched(read[Seq[User]](xhr.responseText)))
        .recover({ case ex => UsersFetchedFailed(ex.getMessage) }))
  }
}

// Selector
trait UserSelector extends GenericConnect[AppModel, Seq[User]] {

  def getUsers() = model

  val cursor = AppCircuit.userSelector
  val circuit = AppCircuit
  connect()
}
