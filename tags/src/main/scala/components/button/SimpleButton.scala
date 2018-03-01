package components.button

import components.Components.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement}
import com.thoughtworks.binding.dom
import components.icon.IconBuilder

case class SimpleButtonBuilder() extends ButtonBaseBuilder {
  def render = this
  var icon: IconBuilder = _

  // TODO find out if icon size is variable

  @dom def build = {

    val button =
      <a class={ className }>   
        { unwrapElement(
          <span class={ getClassName(ICON, SIZE_CLASS) }>
            { unwrapBuilder(icon, icon != null).bind }
          </span>, icon != null).bind }
        <span>{ label }</span>
      </a>.asInstanceOf[HTMLElement]

    button.addEventListener("click", handleOnclick)
    button.addEventListener("click", (_: Event) => onClick())

    button
  }
}
