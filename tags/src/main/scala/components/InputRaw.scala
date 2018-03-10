package components
import components.core.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLInputElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.Binding.Vars
import components.core.ComponentBuilder

case class InputBuilderRaw() extends ComponentBuilder {
      def render = this      
      var label: String = _
      var placeholder: String = _
      var onChange: String => Unit = _
      var isDisabled: () => Binding[Boolean] = _
      
      @dom def build = {
        InputRaw(label.bind, onChange.bind, isDisabled().bind, placeholder.bind).bind.asInstanceOf[HTMLElement]
      }
}

//TODO make private
case class InputRaw private(
    label: String, 
    onchange: String => Unit,
    disabled: Boolean,
    placeholder: String
    ) {
  
  private val value = Var("")
  
  private def handleChange = (e: Event) => {
    value.value = e.currentTarget.asInstanceOf[HTMLInputElement].value
    onchange(value.value)
  }
  
  @dom def render:Binding[HTMLElement] = {
    
    val input = <input 
									value={value.bind} 
									class="input" 
									type="text" 
									placeholder={placeholder}/>.asInstanceOf[HTMLInputElement]
    input.disabled = disabled
    input.oninput = handleChange
    val inputLabel = <label class="label" for={label}> {label} </label>;
    
    <div> 
      {inputLabel} {input} 
		</div>.asInstanceOf[HTMLElement]
  }
}

//TODO make private
case object InputRaw {
  def apply(label: String, 
      onchange: String => Unit, 
      disabled: Boolean = false, 
      placeholder: String = ""):Binding[HTMLElement] = {
    new InputRaw(label, onchange, disabled, placeholder).render
  }
}

