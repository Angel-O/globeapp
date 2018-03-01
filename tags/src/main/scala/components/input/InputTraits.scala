package components.input

import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw.Event
import com.thoughtworks.binding.dom

trait Icons extends LeftIcon with RightIcon

//TODO restrict icons type
trait LeftIcon {
  var leftIcon: HTMLElement = _
}

//TODO add support for right icon in mixin in classes
trait RightIcon {
  var rightIcon: HTMLElement = _
}

trait InputBase {
  var label: String = _
  var labelStyle: String = _
  var isDisabled: Boolean = false
  @dom lazy val labelElement =
    <label class="label" style={labelStyle}>{ label }</label>
}

trait Selection {
  var onSelect: String => Unit = _
  protected var selectedItem: Var[String] = Var("")
  protected val handleSelectionChange: Event => Unit
}
