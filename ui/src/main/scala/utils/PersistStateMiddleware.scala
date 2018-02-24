package utils
import scala.scalajs.js, js.JSON
import org.scalajs.dom.window
import upickle.default._
import appstate.Storage

object PersistStateMiddleware {
  def persist(state: Storage) = {
    val json = write(state)
    window.sessionStorage.setItem("State", json)
  }

  def retrieve() = {
    val json = window.sessionStorage.getItem("State")
    json match {
      case null => Storage()
      case _    => read[Storage](json)
    }
  }

  def wipe() = {
    window.sessionStorage.removeItem("State")
  }
}
