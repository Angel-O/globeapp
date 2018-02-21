package components.modal

import components.Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{dom, Binding}, Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLHRElement

case class SimpleModalBuilder() extends {
  val targetId = s"simpleModal_${SimpleModalBuilder.getId}" //using scala early initializers!!!
  val modalContentClassName = "modal-content" // constant from bulmacss classes is not visible here!!
} with ModalBase{
  def render = this
  
  @dom def build = {

    val triggerClass = getClassName(
        BUTTON, 
        MODAL_BUTTON, 
        (isPrimary, PRIMARY), 
        (sizeIsSet, SIZE_CLASS))
        
    val modalTrigger = <a class={triggerClass} data:data-target={targetId}>
                         { label }
                       </a>.asInstanceOf[HTMLElement] 
     
    //TODO add size modifier for close button
    val closeButton = <button class={MODAL_CLOSE} data:aria-label="close"></button>.asInstanceOf[HTMLElement]
    
    val hideTrigger = !openAtLaunch //TODO make unwrapElement method more idiomatic... 
                       
    modalTrigger.addEventListener("click", launchModal)                                  
    closeButton.addEventListener("click", closeModal)
    
    enableSmartClose()

      <div>
        { unwrapElement(modalTrigger, hideTrigger).bind }
        <div class={modalClass} id={ targetId }>
          <div class="modal-background"></div>
					<div class="modal-content">
    				{ content }
  				</div>
					{ unwrapElement(closeButton, smartClose).bind }
        </div>
      </div>//.asInstanceOf[HTMLElement]
  }
}

case object SimpleModalBuilder {
  var lastAssignedId: Int = _
  def getId() = {
    lastAssignedId += 1
    lastAssignedId
  }
}