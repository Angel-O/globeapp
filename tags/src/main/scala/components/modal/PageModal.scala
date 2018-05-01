package components.modal

import components.core.Implicits._
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLImageElement,
  HTMLButtonElement
}
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLHRElement
import components.icon.IconBuilder

case class PageModalBuilder() extends {
  val targetId = s"pageModal_${PageModalBuilder.getId}" //using scala early initializers!!!
  val modalContentClassName = "modal-content" // constant from bulmacss classes is not visible here!!
} with ModalBase {

  def render = this
  var icon: IconBuilder = _
  var isDisabled: Boolean = _

  @dom def build = {

    val triggerClass =
      getClassName(BUTTON, MODAL_BUTTON, COLOR_CLASS, SIZE_CLASS)

    val modalTrigger = <button class={triggerClass} data:data-target={targetId}> 
                        <div style={"display: flex; justify-content: space-between; align-items: center"}> { unwrapElement(
                          <span class={ getClassName(ICON) }>
                            { unwrapBuilder(icon, icon != null).bind }
          							 	 </span>, icon != null).bind } 
                          <span style={"font-size: 1em"}> { label } </span>
												</div>
                       </button>.asInstanceOf[HTMLButtonElement]
                       
    modalTrigger.disabled = isDisabled

    //TODO add size modifier for close button
    val closeButton =
      <button class={MODAL_CLOSE} data:aria-label="close"></button>
        .asInstanceOf[HTMLElement]

    val showTrigger = !isPageModal && label != null //TODO make unwrapElement method more idiomatic...

    modalTrigger.addEventListener("click", launchModal)
    closeButton.addEventListener("click", closeModal)

    enableSmartClose()

    <div>
      { unwrapElement(modalTrigger, showTrigger).bind }
      <div class={ modalClass } id={ targetId }>
        <div class="modal-background"></div>
				<div class="modal-content" style={"background-color: white; padding: 1.5em"}> <!--TODO this is not flexible... -->
    			{ content }
  			</div>
				{ unwrapElement(closeButton, smartClose).bind }
      </div>
    </div>
  }
}

case object PageModalBuilder {
  var lastAssignedId: Int = _
  def getId() = {
    lastAssignedId += 1
    lastAssignedId
  }
}
