package components

import Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{dom, Binding}, Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.document
import components.dropdown.DropdownBuilder
import org.scalajs.dom.raw.HTMLHRElement

case class NavbarBuilder() extends ComponentBuilder {
  def render = this

  var logo: NavbarLogoBuilder = _
  var isFixedBottom: Boolean = false
  var isFixedTop: Boolean = false
  var isTransparent: Boolean = false
  var leftItems: Seq[NavbarItemBuilder] = Seq.empty //default values for collections
  var rightItems: Seq[NavbarItemBuilder] = Seq.empty //default values for collections
  
  //TODO, use recursion to make it more reliable
  @dom def adjustDropDirection(items: BindingSeq[NavbarItemBuilder]) = {
    val adjusted = items.map(x => {
      if(x.dropdownItems.length.bind > 0) { x.hasDropup = true }
        
      val innerItem = x.item
      innerItem match {
        case x: DropdownBuilder => x.hasDropup = true
        case x: Seq[_] => { 
          val dropdowns = x.filter(y => y.isInstanceOf[DropdownBuilder])
          dropdowns.foreach(_.asInstanceOf[DropdownBuilder].hasDropup = true)         
        }
        case _ => innerItem
      }     
      x
    })
    
    adjusted
  }

  @dom def build = {
    var leftItems = toBindingSeq(this.leftItems)
    var rightItems = toBindingSeq(this.rightItems)
    val navBarHasFixedLocation = isFixedBottom ^ isFixedTop
    val navBarLocation = if (isFixedTop) FIXED_TOP else FIXED_BOTTOM
    val className = getClassName(
        (true, NAVBAR), 
        (navBarHasFixedLocation, navBarLocation),
        (isTransparent, TRANSPARENT))
    
    if(navBarHasFixedLocation && isFixedTop){
      val html = document.getElementsByTagName("HTML").head.asInstanceOf[HTMLElement]
      html.classList.add("has-navbar-fixed-top")
    }
    if(navBarHasFixedLocation && isFixedBottom){
      val html = document.getElementsByTagName("HTML").head.asInstanceOf[HTMLElement]
      html.classList.add("has-navbar-fixed-bottom")
    }

    val toggleMenu = (e: Event) => {
      val self = e.currentTarget.asInstanceOf[HTMLElement]
      val menu = self.parentElement.parentElement
        .getElementsByClassName("navbar-menu").head.asInstanceOf[HTMLElement]
      self.classList.toggle(ACTIVE)
      menu.classList.toggle(ACTIVE)
    }

    //TODO decide what to do with data-target attribute...not needed currently
    val toggle = <button class="button navbar-burger" data:data-target="navMenu">
                   <span></span>
                   <span></span>
                   <span></span>
                 </button>.asInstanceOf[HTMLElement]
    toggle.addEventListener("click", toggleMenu)
    
    if(isFixedBottom){
      //adjust "drop" direction
      leftItems = adjustDropDirection(leftItems).bind
      rightItems = adjustDropDirection(rightItems).bind
    }
    
    <nav class={className} data:role="navigation" data:aria-label="main navigation">
      <div class="navbar-brand">
        { logo.bind }
        { toggle }
      </div>
      <div class="navbar-menu" id="navMenu">
        <div class="navbar-start">
          { leftItems.flatMap(_.bind) }
        </div>
        <div class="navbar-end">
          { rightItems.flatMap(_.bind) }
        </div>
      </div>
    </nav>.asInstanceOf[HTMLElement]
  }
}

case class NavbarLogoBuilder() extends ComponentBuilder() {
  def render = this

  var image: HTMLImageElement = _
  var href: String = _

  @dom def build = {
    <a class="navbar-item" href={ href }>
      { image }
    </a>.asInstanceOf[HTMLElement]
  }
}

case class NavbarItemBuilder() extends ComponentBuilder() {
  def render = this

  var item: Any = _
  var isHoverable: Boolean = false
  var isRightDropdown: Boolean = false
  var hasDropup: Boolean = false //if the navbar is fixed at the bottom, this will be automatically set to true
  var isBoxed: Boolean = true
  var hasDividers: Boolean = false
  var isExpanded: Boolean = false
  var dropdownItems: Seq[Any] = Seq.empty
  
  @dom private def createElement(element: Any): Binding[HTMLElement] = {

    // NOTE with a val this would work... with a def not
    val hasDropdown = dropdownItems.nonEmpty
    
    @dom def createDropdownElement(item: Any) = {
      
      val needsDivider = hasDividers && 
      dropdownItems.indexOf(item) != dropdownItems.length - 1
      
      <div>
        { <NavbarItem item={ item }/>.asInstanceOf[ComponentBuilder].build.bind }
			  {
  			  if (needsDivider)
  			     <hr class="navbar-divider"/>.asInstanceOf[HTMLElement]
  			  else 
  			    DummyBuilder.build.bind
  			}
			</div>.asInstanceOf[HTMLElement]
    }
    
    lazy val className = getClassName(
        (true, "navbar-item"), 
        (hasDropdown, "has-dropdown"),
        (hasDropup, "has-dropdown-up"),
        (isExpanded, EXPANDED),
        (isHoverable, HOVERABLE))
    
    val elem = (element, hasDropdown) match {
      case (x: HTMLHRElement, _) => x
      case (x: HTMLElement, false) => {
         <div class={className}>{x}</div>
      }
      case (x: String, true) => {
        val clickHandler = (e: Event) => e.currentTarget.asInstanceOf[HTMLElement].classList.toggle(ACTIVE)
        val item = <div class={ className }>
                     {
                       val className = getClassName(
                           (true, "navbar-dropdown"), 
                           (isRightDropdown, RIGHT),
                           (isBoxed, BOXED))
                       
                       // TODO fix this when dropdown items will accept more than strings...
                       val dropdownElements: BindingSeq[HTMLElement] =
                         Vars(<a class="navbar-link"> { x } </a>,
                              <div class={ className }>
                               { toBindingSeq(dropdownItems).map(x => createDropdownElement(x).bind) }
                              </div>)
                              
                       dropdownElements.all.bind
                     }
                   </div>.asInstanceOf[HTMLElement]
         if (!isHoverable) item.addEventListener("click", clickHandler)
         item
      }
      case (x: String, false) => {       
        <div class={className}>
        	<a>{x}</a>
        </div>
      }
      case (_: HTMLElement, true) => 
        //TODO this is two restrictive...what about images or a mix of text and images???
        throw new IllegalArgumentException("HTML elements cannot have a dropdown") 
      case _ => 
        throw new IllegalArgumentException("Invalid combo element/dropdown") //TODO improve error msg
    }

    elem.asInstanceOf[HTMLElement]
  }

  @dom private def getItem(genericItem: Any): Binding[HTMLElement] = genericItem match {
    case x: HTMLHRElement =>
      if(!hasDividers) x
      else throw new IllegalArgumentException("Dividers already set")
    case _: String | _: HTMLElement => createElement(genericItem).bind
    case x: Binding[Any] => getItem(x.bind).bind
    case x: ComponentBuilder => getItem(x.build.bind).bind
    case _ => throw new IllegalArgumentException("Invalid item type")
  }

  @dom def build = {
    getItem(item).bind
  }
}