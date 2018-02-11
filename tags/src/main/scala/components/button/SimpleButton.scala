package components.button

import components.Components.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement }
import com.thoughtworks.binding.dom

case class SimpleButtonBuilder() extends ButtonBaseBuilder {
  def render = this
  var icon: HTMLElement = _ //TODO make it safe

  // TODO find out if icon size is variable
  
  @dom def build = {

    val button =
      <a class={ className }>
        <span class={getClassName((true, ICON), (true, SMALL))}>{ icon }</span>
        <span>{ label }</span>
      </a>.asInstanceOf[HTMLElement]

    button.addEventListener("click", handleOnclick)
    button.addEventListener("click", (_: Event) => onClick())

    button
  }
}