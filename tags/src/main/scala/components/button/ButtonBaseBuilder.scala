package components.button

import components.Components.Implicits._
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLButtonElement}
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.Binding.Vars

protected abstract class ButtonBaseBuilder()
    extends ComponentBuilder
    with Color {
  var label: String = _
  var onClick: () => Unit = () => () //do nothing by default

  protected def handleOnclick = (e: Event) => {
    //val self = e.currentTarget.asInstanceOf[HTMLElement]
    //self.classList.toggle(FOCUSED)
  }

  // using lazy val rather than val...
  protected lazy val className =
    getClassName((true, BUTTON), (isPrimary, PRIMARY))
}
