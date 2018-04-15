package components.layout

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Color
import components.core.Size
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLButtonElement,
  HTMLAnchorElement
}
import com.thoughtworks.binding.dom

case class MessageBuilder() extends ComponentBuilder with Color with Size {
  def render = this

  var header: String = _
  var content: HTMLElement = _ //TODO allow for more types
  var style: String = _

  // allows to set it from outside when the message is used as a child component of a component that can set the color
  var colorClass: String = _

  @dom def build = {

    val definitiveColorclass = Option(colorClass).getOrElse(COLOR_CLASS)

    val element =
      <div class={getClassName(MESSAGE, definitiveColorclass, SIZE_CLASS)} style={style}>
            <div class={MESSAGE_HEADER}>{ header }</div>
            <div class={MESSAGE_BODY}>
                { content }
            </div>
        </div>

    Option(style).foreach(css => element.style = css)

    element
  }
}
