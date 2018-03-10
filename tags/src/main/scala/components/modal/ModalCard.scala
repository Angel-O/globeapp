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
import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement

//using scala early initializers!!!
case class ModalCardBuilder() extends {
  val targetId = s"modalCard_${ModalCardBuilder.getId}"
  val modalContentClassName = "modal-card"
} with ModalBase {
  def render = this
  var title: String = _
  var onSave: () => Unit = _

  @dom def build = {

    val triggerClass =
      getClassName(BUTTON, MODAL_BUTTON, COLOR_CLASS, SIZE_CLASS)
    val modalTrigger = <a class={triggerClass} data:data-target={targetId}>
                         { label }
                       </a>
    val saveButton =
      <button class={getClassName(BUTTON, SUCCESS)}>Save changes</button>
    val cancelButton =
      <button class={getClassName(BUTTON)}>Cancel</button>
    val closeButton =
      <button class={getClassName(DELETE)} data:aria-label="close"></button>

    modalTrigger.addEventListener("click", launchModal)
    saveButton.addEventListener("click", closeModal)
    saveButton.addEventListener("click", (e: Event) => onSave()) //TODO this should be handled asyncronously
    cancelButton.addEventListener("click", closeModal)
    closeButton.addEventListener("click", closeModal)

    enableSmartClose()

    <div>
        { modalTrigger }
        <div class={ modalClass } id={ targetId }>
          <div class="modal-background"></div>
          <div class="modal-card">
            <header class="modal-card-head">
              <p class="modal-card-title">{ title }</p>
              { unwrapElement(closeButton, smartClose).bind }
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
      </div>
  }
}

case object ModalCardBuilder {
  var lastAssignedId: Int = _
  def getId() = {
    lastAssignedId += 1
    lastAssignedId
  }
}
