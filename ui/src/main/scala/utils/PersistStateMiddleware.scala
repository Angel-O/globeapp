package utils
import scala.scalajs.js, js.JSON
import org.scalajs.dom.window
import upickle.default._
import appstate.PersistentState

object PersistStateMiddleware {
  def persist(state: PersistentState) = {
    val json = write(state)
    window.sessionStorage.setItem("State", json)
  }

  def retrieve() = {
    val json = window.sessionStorage.getItem("State")
    json match {
      case null => PersistentState()
      case _    => read[PersistentState](json)
    }
  }

  def wipe() = {
    window.sessionStorage.removeItem("State")
  }
}
