package components.dropdown

import components.core.Implicits._
import components.core.Helpers._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLButtonElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding.BindingSeq
import components.core.ComponentBuilder
import components.Components.CustomTags2

case class DropdownBuilder() extends ComponentBuilder{
  def render = this
  var label: String = _
  //TODO improve automatically setting this to true if the navbar is fixed at the bottom...see navbar
  var hasDropup: Boolean = false 
  var isHoverable: Boolean = true
  var isRight: Boolean = false
  var menuItems: Seq[Any] = _ //TODO accommodate for binding elements and html elements

  private val toggleDropdown = (e: Event) => {
    val dropdown = e.currentTarget.asInstanceOf[HTMLElement].parentElement.parentElement
    dropdown.classList.toggle(ACTIVE)
  }
  
  private val resetActiveMenuItems = (e: Event) => {
    val dropdown = e.currentTarget.asInstanceOf[HTMLElement].parentElement.parentElement
    val menu = dropdown.children.last
    val menuContent = menu.children.head
    val menuItems = menuContent.children
    menuItems.filter(_.classList.contains(ACTIVE)).foreach(_.classList.remove(ACTIVE))
  }

  @dom def build = {

    val toggle = <button class="button" data:aria-haspopup="true" data:aria-controls="dropdown-menu">
                   <span>{ label }</span>
                   <span class="icon is-small">
                     <i class={s"fa fa-angle-${if(hasDropup) "up" else "down"}"} data:aria-hidden="true"></i>
                   </span>
                 </button>.asInstanceOf[HTMLButtonElement]

    if(!isHoverable) { toggle.addEventListener("click", toggleDropdown) }
    toggle.addEventListener(s"${if(!isHoverable)"click" else "mouseleave"}", resetActiveMenuItems)
    
    val menuElements = toBindingSeq(menuItems)
                        .map(x => <MenuItem itemText={x.toString}/>.asInstanceOf[MenuItemBuilder])

    val className = getClassName(
        (true, DROPDOWN), 
        (hasDropup, IS_UP),
        (isHoverable, HOVERABLE),
        (isRight, RIGHT))
        
    val dropdownContent = <div class="dropdown-content">
                            { menuElements.flatMap(_.bind) }
                          </div>.asInstanceOf[HTMLElement]
    
    if(isHoverable){
      dropdownContent.addEventListener("mouseleave", resetActiveMenuItems)
    }
        
    val menu = <div class={className}>
                 <div class="dropdown-trigger">
                   { toggle }
                 </div>
                 <div class="dropdown-menu" id="dropdown-menu" data:role="menu">
									 { dropdownContent }
                 </div>
               </div>.asInstanceOf[HTMLElement]
               
    menu
  }
}