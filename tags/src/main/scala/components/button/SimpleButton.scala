package components.button

import components.core.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLButtonElement}
import com.thoughtworks.binding.dom
import components.icon.IconBuilder

case class SimpleButtonBuilder() extends ButtonBaseBuilder {
  def render = this
  var icon: IconBuilder = _

  // TODO find out if icon size is variable
  var isDisabled: Boolean = _

  @dom def build = {

    val button =
      <button class={ className }>   
        { unwrapElement(
          <span class={ getClassName(ICON, SIZE_CLASS) }>
            { unwrapBuilder(icon, icon != null).bind }
          </span>, icon != null).bind }
        <span>{ label }</span>
      </button>.asInstanceOf[HTMLButtonElement]

    button.addEventListener("click", handleOnclick)
    button.addEventListener("click", (_: Event) => onClick())
    button.disabled = isDisabled

    button
  }
}
