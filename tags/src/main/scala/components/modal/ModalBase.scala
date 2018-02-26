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
import org.scalajs.dom.raw.HTMLHRElement
import org.scalajs.dom.raw.HTMLElement

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
    if (smartClose) onSmartClose()
  }

  private val closeOnClickOutside = () => {

    // Note: this function will be executed as part of the even handler
    // returning the only the modal-card portion as an open Bulma modal background
    // occupies the whole page, but we only need to detect clicks outside the modal body
    // Note: the modal might be null for some weird reasons, hence the need to wrap he result
    // into an Option...probably a timing issue when the component is re-mounted
    def getModal: Option[HTMLElement] = {
      val modal = Option(document.querySelector(s"#${targetId}"))

      val modalBody: Option[HTMLElement] = modal
        .map(_.getElementsByClassName(modalContentClassName).head)

      modalBody
    }
    // using target rather than currentTarget in combination
    // with useCapture to get the specific element that was clicked.
    // (need to set useCapture to true to get the actual element)
    document.addEventListener(
      "click",
      (e: Event) => {
        getModal.map { modal =>
          if (isOpen.value && !modal.contains(e.target)) closeModal(e)
        }
      },
      true
    )
  }
}
