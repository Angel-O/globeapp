package utils
import scala.scalajs.js, js.JSON
import org.scalajs.dom.window
import appstate.PersistentState
import play.api.libs.json.OFormat
import play.api.libs.json.Json
import api.{write, read} 
import json.toJsonValue

object PersistStateMiddleware {
  
  implicit val persistentStateFormat: OFormat[PersistentState] = Json.format[PersistentState]
  def persist(state: PersistentState) = {
    val json = write(state)
    window.sessionStorage.setItem("State", json)
  }

  def retrieve() = {
    val maybeJson = Option(window.sessionStorage.getItem("State")) 
    maybeJson.map(json => read[PersistentState](json))
  }

  def wipe() = {
    window.sessionStorage.removeItem("State")
  }
}
