package components
import Components.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLInputElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var

case class InputBuilder() extends ComponentBuilder {
      def render = this
      var label: String = _
      var placeholder: String = _
      var onChange: String => Unit = _
      var isDisabled: () => Binding[Boolean] = _ // binding because you tipically want to subscribe to external events
      
      private var value: Var[String] = Var("") // manage internal state: controlled component
      private def handleChange = (e: Event) => {
          value.value = e.currentTarget.asInstanceOf[HTMLInputElement].value
          onChange(value.value)
      }  
      
      //TODO remove this
      @dom val className = if (value.bind == "ciao") "is-success" else "is-danger"
      
      @dom def build = {
          val input = <input 
									value={value.bind} 								 
									class={s"input ${className.bind}"} 
									type="text" 
									placeholder={placeholder}/>.asInstanceOf[HTMLElement]
          input.disabled = isDisabled().bind 
          // attaching it here rather that directly as an attribute to avoid error marks with Eclipse
          input.oninput = handleChange
          val inputLabel = <label class="label" for={label}> {label} </label>;
    
          <div> 
            {inputLabel} {input} 
					</div>.asInstanceOf[HTMLElement]
      }
}