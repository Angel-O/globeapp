package components.modal

import components.Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{dom, Binding}, Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement

//using scala early initializers!!!
case class ModalCardBuilder() extends{ val targetId = s"modalCard_${ModalCardBuilder.getId}" 
                                       val modalContentClassName = "modal-card" } with ModalBase(){
  def render = this
  var title: String = _ 
  var onSave: () => Unit = _
  
  @dom def build = {
    
    val triggerClass = getClassName(
        (true, getClassTokens(BUTTON, MODAL_BUTTON)), 
        (isPrimary, PRIMARY), 
        (sizeIsSet, SIZE_CLASS))
    val modalTrigger = <a class={triggerClass} data:data-target={targetId}>
                         { label }
                       </a>.asInstanceOf[HTMLElement] 
    val saveButton = <button class={getClassTokens(BUTTON, SUCCESS)}>Save changes</button>.asInstanceOf[HTMLElement]  
    val cancelButton = <button class={getClassTokens(BUTTON)}>Cancel</button>.asInstanceOf[HTMLElement]  
    val closeButton = <button class={getClassTokens(DELETE)} data:aria-label="close"></button>.asInstanceOf[HTMLElement]
                       
    modalTrigger.addEventListener("click", launchModal)                   
    saveButton.addEventListener("click", closeModal)
    saveButton.addEventListener("click", (e: Event) => onSave()) //TODO this should be handled asyncronously
    cancelButton.addEventListener("click", closeModal)                   
    closeButton.addEventListener("click", closeModal)
    
    enableSmartClose()

      <div>
        { modalTrigger }
        <div class="modal" id={ targetId }>
          <div class="modal-background"></div>
          <div class="modal-card">
            <header class="modal-card-head">
              <p class="modal-card-title">{ title }</p>
              { closeButton }
            </header>
            <section class="modal-card-body">
              { content }
            </section>
            <footer class="modal-card-foot" style={ "justify-content: flex-end" }>
              { saveButton }
              { cancelButton }
            </footer>
          </div>
        </div>
      </div>.asInstanceOf[HTMLElement]
  }
}

case object ModalCardBuilder {
  var lastAssignedId: Int = _
  def getId() = {
    lastAssignedId += 1
    lastAssignedId
  }
}