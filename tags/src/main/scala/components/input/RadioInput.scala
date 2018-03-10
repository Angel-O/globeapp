package components.input

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Helpers._
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLImageElement,
  HTMLButtonElement
}
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement
import org.scalajs.dom.raw.HTMLSelectElement
import org.scalajs.dom.raw.HTMLInputElement

case class RadioInputBuilder()
    extends ComponentBuilder
    with InputBase
    with Selection {
  def render = this
  var options: Seq[String] = Seq.empty
  val inputType = RadioInput
  var name: String = _ // Server name...TODO do I need this???

  override protected val handleSelectionChange = (e: Event) => {
    val radioButton = e.currentTarget.asInstanceOf[HTMLInputElement]
    selectedItem.value = radioButton.value
    onSelect(selectedItem.value)
  }

  //TODO throw error for duplicate options
  @dom override def build = {

    val fieldClassName = getClassName((true, FIELD))
    val optionItems = toBindingSeq(this.options)

    val labelItem = unwrapElement(labelElement.bind, label != null)

    <div class={ fieldClassName }>
      <div class="control">
				{ labelItem.bind }
        {
          optionItems.map(x => <label class={ inputType.name } style={ labelStyle }>
                                 { 
                                   val radioButton = <input type={ inputType.name } name={ name } value={ x }/>
                                   radioButton.onchange = handleSelectionChange 
                                   radioButton 
                                 }
                                 { x }
                               </label>)
        }
      </div>
    </div>.asInstanceOf[HTMLElement]
  }
}
