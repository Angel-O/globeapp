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
    val maybeJson = Option(window.sessionStorage.getItem("State")) 
    maybeJson.fold(PersistentState())(json => read[PersistentState](json))
  }

  def wipe() = {
    window.sessionStorage.removeItem("State")
  }
}
