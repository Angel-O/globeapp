package components.input

import components.Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{ dom, Binding }, Binding.{ Var, Vars, Constants, BindingSeq }
import org.scalajs.dom.document
import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement
import org.scalajs.dom.raw.HTMLSelectElement
import org.scalajs.dom.raw.HTMLInputElement


import scalajs.js
import org.scalajs.dom.raw.MutationObserverInit
import org.scalajs.dom.raw.HTMLParagraphElement
import org.scalajs.dom.raw.MutationObserver
import org.scalajs.dom.raw.MutationRecord
import org.scalajs.dom.raw.Node
import org.scalajs.dom.raw.HTMLTextAreaElement

case class TextInputBuilder() extends { val inputType = TextInput } with TextualInput {
  
  def render = this
  
  var onChange: String => Any = _

  // without using this property the value of the input field would be
  // out of sync with what the user sees: the cursor would reset to
  // its position all the way to the left when the input box loses
  // focus
  var inputValue: String = _  
  
  //TODO remove this...it is not needed
  //private var value: Var[String] = Var("") // manage internal state: controlled component
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     //value.value = target.value
     onChange(target.value) //TODO use target.value
  }
  
  @dom override def build = {
    
    val input = inputElement.bind.asInstanceOf[HTMLInputElement]
    input.oninput = handleChange
    input.value = this.inputValue
     
    super.build.bind
  }
}

case class EmailInputBuilder() extends { val inputType = EmailInput } with TextualInput {
  def render = this
  
  var onChange: String => Unit = _
  var inputValue: String = _
  
  private var value: Var[String] = Var("") // manage internal state: controlled component
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     value.value = target.value
     onChange(value.value)
  }
  
  @dom override def build = {
    
    val input = inputElement.bind.asInstanceOf[HTMLInputElement]
    input.oninput = handleChange
    input.value = this.inputValue
     
    super.build.bind
  }
}

case class PasswordInputBuilder() extends { val inputType = PasswordInput } with TextualInput {
  def render = this
  
  var onChange: String => Unit = _
  var inputValue: String = _
  
  private var value: Var[String] = Var("") // manage internal state: controlled component
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     value.value = target.value
     onChange(value.value)
  }
  
  @dom override def build = {
    val input = inputElement.bind.asInstanceOf[HTMLInputElement]
    input.oninput = handleChange
    input.disabled = isDisabled
    input.value = this.inputValue
        
    super.build.bind
  }
}

case class TextareaInputBuilder() extends { val inputType = TextareaInput } with TextualInput {
  def render = this
  
  var onChange: String => Unit = _
  var inputValue: String = _
  
  private var value: Var[String] = Var("") // manage internal state: controlled component
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     value.value = target.value
     onChange(value.value)
  }
  //TODO text area needs no icons
  @dom override def build = {
    
    val fieldClassName = getClassName((true, FIELD))

    val inputElement = <textarea class={ inputType.name } placeholder={ placeHolder }/>.asInstanceOf[HTMLTextAreaElement]
    inputElement.bind.disabled = isDisabled
    inputElement.oninput = handleChange
    inputElement.value = inputValue

    <div class={ fieldClassName }>
      { labelElement.bind }
      <div class="control">
        { inputElement.bind }
      </div>
    </div>.asInstanceOf[HTMLElement]
  }
}

sealed trait TextualInput extends ComponentBuilder with InputBase with Icons{
  var placeHolder: String = ""

  val inputType: InputType
  @dom protected val inputElement = {
    
    val input = <input class="input" type={ inputType.name } placeholder={ placeHolder }/>
    
    input.asInstanceOf[HTMLElement]
  }
  
  @dom def build = {
    
    //TODO add support for multiple sizes 
    val leftIconElement = <span class="icon is-small is-left">
                            { Option(leftIcon).getOrElse(dummy.build.bind) }
                          </span>

    val fieldClassToken = if (leftIcon != null) HAS_ICONS_LEFT else ""
    val fieldClassName = getClassName((true, FIELD), (true, fieldClassToken))
   
    val control = <div class="control">
                    { inputElement.bind }
                    { unwrapElement(leftIconElement, leftIcon != null).bind }
                  </div>

    var element = <div class={ fieldClassName }>
                    { labelElement.bind }
                    { control }
                  </div>.asInstanceOf[HTMLElement]
     element
  }
}

case class FieldValidationBuilder() extends ComponentBuilder{
  def render = this
  var errorMessage: String = ""
  var successMessage: String = ""
  
  @dom def build = {
    val messageText = if (errorMessage.nonEmpty) errorMessage else successMessage
    val messageClassToken = if (errorMessage.nonEmpty) DANGER else SUCCESS
    val messageClassName = getClassName((true, HELP), (true, messageClassToken))
    val element = <p class={ messageClassName }>{ messageText }</p>
    val message = unwrapElement(element, messageText.nonEmpty).bind
    
    message
  }
}


//      var observer = new MutationObserver(
//        (mutations: js.Array[MutationRecord], obs: MutationObserver) => 
//          mutations.foreach(x => {
//            val item = x.addedNodes.item(0)
//            if(item != null && item.isInstanceOf[HTMLParagraphElement]){
//              println("Value bef", item.asInstanceOf[HTMLParagraphElement].textContent)
//              item.childNodes.foreach(x => println(x.textContent))
//              val parent = item.asInstanceOf[HTMLParagraphElement].parentElement
//              item.textContent = "Mutation mate"
//              println("Value aft", item.asInstanceOf[HTMLParagraphElement].innerHTML)
//              
//              if(parent != null){
//                parent.removeChild(item)
//                //all = document.createElement("div").asInstanceOf[HTMLElement]
//              }  
//            }
//            if(item.isInstanceOf[HTMLInputElement]){
//              println("Value before", item.asInstanceOf[HTMLInputElement].value)
//              item.asInstanceOf[HTMLInputElement].value = "Mutation mate"
//              println(item.asInstanceOf[HTMLInputElement].value)
//              item.nextSibling.textContent = "Mutation mate"
//              println("Value after", item.asInstanceOf[HTMLInputElement].value)
//            }
//              
//            }))
//    
//    val contentObserverParams = new js.Object{
//        val subtree = true
//        val attributes = true
//        val childList =true
//        val characterData = true
//        val characterDataOldValue =true
//      }.asInstanceOf[MutationObserverInit]
    
//      val node = document.getElementById("Hello").asInstanceOf[Node]
//      println("NODE, ", node)
//      if (node != null){
//        observer.observe(node, contentObserverParams)
//      }
      
      //observer.observe(document.body, contentObserverParams)