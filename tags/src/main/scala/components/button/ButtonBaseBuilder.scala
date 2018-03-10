package components.button

import org.scalajs.dom.raw.Event

import components.core.Color
import components.core.ComponentBuilder
import components.core.Size

protected abstract class ButtonBaseBuilder
    extends ComponentBuilder
    with Color
    with Size {

  var label: String = _
  var onClick: () => Unit = () => () //do nothing by default

  protected def handleOnclick = (e: Event) => {
    //val self = e.currentTarget.asInstanceOf[HTMLElement]
    //self.classList.toggle(FOCUSED)
  }

  def className = getClassName(COLOR_CLASS, BUTTON, SIZE_CLASS)
}
