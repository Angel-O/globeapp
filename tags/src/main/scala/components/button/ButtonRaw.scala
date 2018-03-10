package components.button

import components.core.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLButtonElement}
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var

case class ButtonBuilderRaw() extends ButtonBaseBuilder {
  def render = this
  var isDisabled: () => Binding[Boolean] = _

  @dom def build =
    ButtonRaw(label, onClick, isDisabled().bind, isPrimary).bind
      .asInstanceOf[HTMLElement]
}

//TODO make private
case class ButtonRaw private (
    label: String,
    onClick: () => Unit,
    disabled: Boolean,
    isPrimary: Boolean
) {

  // using a Var rather than a simple boolean flag to re-render the button
  private var clicked = Var(false)

  private def handleOnclick = (e: Event) => {
    clicked.value = !clicked.value
    onClick()
  }

  @dom private def className =
    s"button ${if (clicked.bind) "is-focused"} ${if (isPrimary) "is-primary"}"

  @dom private def render: Binding[HTMLElement] = {

    // need to declare a HTMLButtonElement because the disabled flag cannot be set
    // html tags are treated as xml and require key-value pairs as attributes
    // while disabled is just a "unary" attribute
    val button =
      <button class={className.bind}>
  			{ label }
			</button>.asInstanceOf[HTMLButtonElement]
    button.disabled = disabled
    button.onclick = handleOnclick
    button.asInstanceOf[HTMLElement]
  }
}

//TODO make private
case object ButtonRaw {

  def apply(label: String,
            onclick: () => Unit,
            disabled: Boolean = false,
            primary: Boolean = false): Binding[HTMLElement] = {
    val btn = new ButtonRaw(label, onclick, disabled, primary)
    btn.render
  }
}
