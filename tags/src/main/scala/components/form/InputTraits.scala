package components.form

import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw.Event

trait Icons extends LeftIcon with RightIcon

//TODO restrict icons type
trait LeftIcon{
  var leftIcon: HTMLElement = _
}

//TODO add support for right icon in mixin in classes
trait RightIcon{
  var rightIcon: HTMLElement = _
}

//TODO remove form Item mixin
trait InputBase extends FormItem {  
  var label: String = _
  var isDisabled: Boolean = false
}

trait Selection{
  var onSelect: String => Unit = _
  protected var selectedItem: Var[String] = Var("")
  protected val handleSelectionChange: Event => Unit
}

//TODO remove this
trait FormItem{
  def getValue(): String = "HI"
}