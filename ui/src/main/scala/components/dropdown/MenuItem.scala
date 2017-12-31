package components.dropdown

import components.Components.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLButtonElement, HTMLAnchorElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw.NodeListOf

case class MenuItemBuilder() extends ComponentBuilder with ClickableToggleWithSiblings{
  def render = this
  var itemText: String = ""

  @dom def build = {
    val item = <a href="#" class="dropdown-item">
                 { itemText }
               </a>.asInstanceOf[HTMLAnchorElement]

    item.addEventListener("click", (e: Event) => toggleItem(e, ACTIVE))
    item.addEventListener("click", (e: Event) => deactivateSiblings(e, ACTIVE))
    
    item.asInstanceOf[HTMLElement]
  }
}