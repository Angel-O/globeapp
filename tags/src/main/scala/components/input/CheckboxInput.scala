package components.input

import components.core.Implicits._
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLImageElement,
  HTMLButtonElement,
  HTMLInputElement
}
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import components.core.ComponentBuilder

case class CheckboxInputBuilder() extends ComponentBuilder with InputBase {
  def render = this

  var onSelect: Boolean => Unit = _

  val inputType = CheckboxInput //it's basically irrelevant at the moment... TODO fix this
  private val isSelected = Var(false)

  val handleSelectionChange = (e: Event) => {
    val checkBox = e.currentTarget.asInstanceOf[HTMLInputElement]
    isSelected.value = checkBox.checked
    onSelect(isSelected.value)
  }

  @dom override def build = {

    val fieldClassName = getClassName((true, FIELD))

    <div class={ fieldClassName }>
      <div class="control">
        <label class={ inputType.name }>
          <!--TODO use Bulma classes-->
          { 
            val checkBox = <input type={ inputType.name } value={ label }/> 
            checkBox.onchange = handleSelectionChange
            checkBox
          }
          { <span style={labelStyle}>{label}</span> }<!--TODO allow for html elements, not just strings -->
        </label>
      </div>
    </div>.asInstanceOf[HTMLElement]
  }
}
