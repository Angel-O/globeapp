package components.input

import components.Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{ dom, Binding }, Binding.{ Var, Vars, Constants, BindingSeq }
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLSelectElement
import org.scalajs.dom.raw.HTMLOptionElement

case class SelectInputBuilder() extends ComponentBuilder with InputBase with LeftIcon with Selection {
  def render = this
  var options: Seq[String] = Seq.empty
  var isFullWidth = true
  var isMultiple = false
  val inputType = SelectInput //it's basically irrelevant at the moment...
  var maxSize = 10
  var hasDefaultOption = true
  var defaultOptionText = "Please select"
  var disabledOptions: Seq[Int] = Seq.empty
  
  override protected val handleSelectionChange = (e: Event) => {
    val selectBox = e.currentTarget.asInstanceOf[HTMLSelectElement]
    val selectionIndex = selectBox.selectedIndex.toInt
    val allOptions = if(hasDefaultOption) defaultOptionText +: options else options
    // note we need the val name to start by capital to pattern-match against a constant
    val DEFAULT_OPTION_TEXT = defaultOptionText 
    selectedItem.value = allOptions.find(x => allOptions.indexOf(x) == selectionIndex).get match {
      case DEFAULT_OPTION_TEXT => ""
      case x => x
    }
    
    onSelect(selectedItem.value)
  }

  //TODO throw error if options have duplicates
  @dom override def build = {

    val fieldClassName = getClassName((true, FIELD))
    val optionItems = toBindingSeq(this.options)

    val leftIconElement = <span class="icon is-small is-left">
                            { Option(leftIcon).getOrElse(dummy.build.bind) }
                          </span>.asInstanceOf[HTMLElement]

    val controlClassName = getClassName(
      (true, CONTROL),
      (leftIcon != null, HAS_ICONS_LEFT),
      (isFullWidth, EXPANDED))

    val selectClassName = getClassName(
      (true, inputType.name), //TODO use Bulma class
      (isFullWidth, FULLWIDTH),
      (isMultiple, "is-multiple"))

    val disabledOption = <option> { defaultOptionText } </option>.asInstanceOf[HTMLOptionElement]
    disabledOption.defaultSelected = true
    
    // TODO NOTE: dummy needs to be removed in order to ensure correct working...consider removing it here
    // rather than with a global script
    val selectBox = <select>
											{ unwrapElement(disabledOption, hasDefaultOption).bind } 
                      { optionItems.map(x => { 
                          val option = <option>{ x.bind }</option>
                          if(disabledOptions.contains(options.indexOf(x))){
                            option.disabled = true
                          }
                          option
                        }) 
                      }
                    </select>.asInstanceOf[HTMLSelectElement]

    selectBox.multiple = isMultiple
    selectBox.size = if(isMultiple) options.length min maxSize else 1
    
    selectBox.onchange = handleSelectionChange

    // TODO use Bulma classes
    <div class={ fieldClassName }>
      { labelElement.bind }
      <div class={ controlClassName }>
        <div class={ selectClassName }>
          { selectBox }
          { unwrapElement(leftIconElement, leftIcon != null).bind }
        </div>
      </div>
    </div>.asInstanceOf[HTMLElement]
  }
}
