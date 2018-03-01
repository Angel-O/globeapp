package components.button
import components.Components.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLButtonElement}
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import scala.scalajs.js
import scala.scalajs.js.Any.fromFunction1

case class ButtonBuilder() extends ButtonBaseBuilder() {
  def render = this

  var isDisabled: Boolean = false

  @dom def build = {

    val button =
      <button class={ className }>
        { label }
      </button>.asInstanceOf[HTMLButtonElement]

    button.disabled = isDisabled

    button.addEventListener("click", handleOnclick)
    button.addEventListener("click", (_: Event) => onClick())

    button.asInstanceOf[HTMLElement]
  }
}
