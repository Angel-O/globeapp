package components.button

import components.core.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement}
import com.thoughtworks.binding.dom
import components.icon.IconBuilder

// TODO remove this...it's just a temoorary fix
case class IconButtonBuilder() extends ButtonBaseBuilder {
  def render = this
  var icon: HTMLElement = _

  // TODO find out if icon size is variable

  @dom def build = {

    val button =
      <a class={ className }>   
        { unwrapElement(
          <span class={ getClassName(ICON, SIZE_CLASS) }>
            { unwrapElement(icon, icon != null).bind }
          </span>, icon != null).bind }
        <span>{ label }</span>
      </a>.asInstanceOf[HTMLElement]

    button.addEventListener("click", handleOnclick)
    button.addEventListener("click", (_: Event) => onClick())

    button
  }
}
