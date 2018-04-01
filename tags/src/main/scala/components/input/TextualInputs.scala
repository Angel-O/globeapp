package components.input

import components.core.Implicits._
import components.core.ComponentBuilder
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
import components.core.Color

case class TextInputBuilder() extends { val inputType = TextInput } with TextualInput {
  
  def render = this
  
  var onChange: String => Any = _
  var inputValue: String = _
  
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     onChange(target.value)
  }
  
  @dom override def build = {
    
    val input = inputElement.bind.asInstanceOf[HTMLInputElement]
    input.oninput = handleChange
    Some(inputValue).map(text => input.value = text) 
    super.build.bind
  }
}

case class NumericInputBuilder() extends { val inputType = NumericInput } with TextualInput {
  
  def render = this
  
  var onChange: Int => Any = _
  var min: String = _ 
  var max: String = _
  var inputValue: String = _
  
  private def handleChange = (e: Event) => {
     val target = e.currentTarget.asInstanceOf[HTMLInputElement]
     val numericValue = if(target.value.trim == "") 0 else target.value.toInt

     var cappedValue =
     (for{
       minimum <- Some(min)
       maximum <- Some(max)
     }
     yield(
       if (numericValue > maximum.toInt) maximum.toInt 
       else if(numericValue < minimum.toInt) minimum.toInt
       else numericValue)).getOrElse(0)

     onChange(cappedValue)
  }
  
  @dom override def build = {
    
    val input = inputElement.bind.asInstanceOf[HTMLInputElement]
    input.oninput = handleChange
    input.value = inputValue
    Some(min).map(m => input.min = m)
    Some(max).map(m => input.max = m)
     
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
    
    val fieldClassName = getClassName(FIELD,(isHorizontal, HORIZONTAL)) //TODO horizontal does nothing at the moment

    val inputElement = <textarea class={ inputType.name } placeholder={ placeHolder }/>.asInstanceOf[HTMLTextAreaElement]
    inputElement.bind.disabled = isDisabled
    inputElement.oninput = handleChange
    inputElement.value = inputValue

    <div class={ fieldClassName }>
      { unwrapElement(labelElement.bind, label != null).bind }
      <div class="control">
        { inputElement.bind }
      </div>
    </div>.asInstanceOf[HTMLElement]
  }
}

sealed trait TextualInput extends ComponentBuilder with InputBase with Icons{
  var placeHolder: String = ""

  val inputType: InputType
  @dom protected lazy val inputElement = {
    
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
                    { unwrapElement(labelElement.bind, label != null).bind }
                    { control }
                  </div>.asInstanceOf[HTMLElement]
     element
  }
}

case class FieldValidationBuilder() extends ComponentBuilder with Color{
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