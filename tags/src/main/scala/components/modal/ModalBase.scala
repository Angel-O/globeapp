package components.modal

import components.Components.Implicits.{CustomTags2, _}
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLImageElement,
  HTMLButtonElement
}
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
//import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement

abstract class ModalBase() extends ComponentBuilder with Size {
  var label: String = _ //affects the trigger button
  var content: HTMLElement = _
  var isPrimary: Boolean = _ //affects the trigger button
  var smartClose: Boolean = true
  var openAtLaunch: Boolean = _
  var onSmartClose: () => Unit = () => ()

  protected var isOpen = Var(false)
  val targetId: String
  val modalContentClassName: String

  protected def enableSmartClose() = if (smartClose) { closeOnClickOutside() }

  protected def modalClass = getClassName(MODAL, (openAtLaunch, ACTIVE))

  protected val launchModal = (e: Event) => {
    val target = document.querySelector(s"#${targetId}")
    target.classList.add(ACTIVE)
    isOpen.value = true
  }

  protected val closeModal = (e: Event) => {
    val target = document.querySelector(s"#${targetId}")
    target.classList.remove(ACTIVE)
    isOpen.value = false
    if(smartClose) onSmartClose()
  }

  private val closeOnClickOutside = () => {

    // Note: this function will be executed as part of the even handler
    def getModal: HTMLElement = {
      val modal =
        document.querySelector(s"#${targetId}").asInstanceOf[HTMLElement]
      // returning the only the modal-card portion as an open Bulma modal background
      // occupies the whole page, but we only need to detect clicks outside the modal body
      val modalBody = modal match {
        case null =>
          null // Note the modal might be null for some weird reasons so wee need to check
        // against that...probably timing issue when the component is re-mounted
        case _ =>
          modal
            .getElementsByClassName(modalContentClassName)
            .head
            .asInstanceOf[HTMLElement]
      }
      modalBody
    }
    document.addEventListener(
      "click",
      (e: Event) => {
        val modal = getModal
        // using target rather than currentTarget in combination
        // with useCapture to get the specific element that was clicked.
        if (modal != null && isOpen.value && !modal.contains(
              e.target.asInstanceOf[HTMLElement])) {
          closeModal(e)
        }
      },
      true
    ) // need to set useCapture to true to get the actual element
  }
}
