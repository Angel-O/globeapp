package components.tab

import components.core.Implicits._
import components.core.Helpers._
import org.scalajs.dom.raw.{ Event, HTMLElement}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.dom
import org.scalajs.dom.raw.CustomEvent
import scalajs.js.Dynamic.{ global => g, newInstance => jsnew, literal => lit }
import components.core.ComponentBuilder
import components.core.ClickableToggleWithSiblings
import components.core.Size
import components.Components.Tab
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLAnchorElement
import com.thoughtworks.binding.Binding

case class TabSwitchBuilder() extends ComponentBuilder with ClickableToggleWithSiblings with Size{
  def render = this

  var tabLabels: Seq[Any] = _
  var tabContents: Seq[Any] = _
  var isCentered: Boolean = false
  var isRight: Boolean = false
  var isBoxed: Boolean = true
  var isFullWidth: Boolean = false
  var isRounded: Boolean = false
  var firstTabIsActive: Boolean = true
  //var toggleTabs: Boolean = true NOT WORKING...apart from styling

  private var activeTab = Var(0)
  
  //TODO move this to component builder superclass
  private def makeEvent(tabIndex: Int): CustomEvent = {
    val event = jsnew(g.CustomEvent)("active-tab-changed", lit(detail = tabIndex)).asInstanceOf[CustomEvent]
    event
  }

  private def showContent = (e: Event) => {
    var self = e.currentTarget.asInstanceOf[HTMLElement]
    var index = self.getAttribute("tab-index").toInt
    activeTab.value = index
    
    val ancestor = self.parentElement.parentElement.parentElement
    val contentWrapper = ancestor.children.last.asInstanceOf[HTMLElement]
    val targets = contentWrapper.children
    
    val activeTabChangedEvent = makeEvent(index)
    targets.foreach(_.dispatchEvent(activeTabChangedEvent))
  }

  // defaulting to the left if they're both true or false
  private lazy val notOnLeft = isCentered ^ isRight
  
  private lazy val className = getClassName(
    TABS,
    (notOnLeft, if (isCentered) CENTERED else (RIGHT)),
    SIZE_CLASS,
    (isBoxed, BOXED),
    (isFullWidth, FULLWIDTH),
    (isRounded, ROUNDED))

  @dom private def makeLabel(label: Any, index: Int) = {

    // this function allows to handle any type of label...binding as well!!
    // Note html elements are restricted to anchor tags because that's how Bulma
    // handle tabs...other tags such as divs would look weird
    @dom def getLabel(genericLabel: Any) = genericLabel match {
      case x: String => <a>{x}</a>
      case x: HTMLAnchorElement => x
      case x: Binding[Any] => <a>{x.bind.toString}</a>
      case _ => <a>{genericLabel.toString}</a> //or throw exception ???
    }
    
    val className = getClassName((firstTabIsActive && index == 0, ACTIVE))

    val elem = <li class={ className } data:tab-index={ index.toString }>
                 { getLabel(label).bind }
               </li>.asInstanceOf[HTMLElement]
    elem.addEventListener("click", (e: Event) => toggleItem(e, ACTIVE))
    elem.addEventListener("click", (e: Event) => deactivateSiblings(e, ACTIVE))
    elem.addEventListener("click", showContent)
    elem
  }
  
  @dom private def makeContent(content: Any, index: Int) = {
    
    @dom def getContent(genericContent: Any) = {
      <TabContent content={genericContent}/>.asInstanceOf[TabContentBuilder].build
    }.bind // returns a binding...we need to call bind again
    
    val elem = getContent(content).bind
               
    val attr = document.createAttribute("content-index")
    elem.setAttribute("content-index", index.toString)
    elem.addEventListener("active-tab-changed", handleActiveTabChanged)
    if(index != activeTab.value) {
      elem.classList.add(HIDDEN) // hide on initial load
    }
    elem
  }
  
  private def handleActiveTabChanged = (e: CustomEvent) => {
    val self = e.currentTarget.asInstanceOf[HTMLElement]
    val activeTab = e.detail // could use the Var...(this.activeTab.value)
    val contentIndex = self.getAttribute("content-index")
    if (contentIndex.toInt == activeTab){
      self.classList.remove(HIDDEN)
    }
    else{
      self.classList.add(HIDDEN)
    }
    removeClassAttributeIfEmpty(self)
  }
  
//  @dom private def renderContent() = {
//    
//    val elems = content.flatMap(_.bind).all.bind
//    elems.foreach(x => {
//      val attr = document.createAttribute("content-index")
//      val index =  elems.indexOf(x)
//      x.setAttribute("content-index", index.toString)
//      x.addEventListener("active-tab-changed", handleActiveTabChanged)
//      if(index != activeTab.value) {
//        x.classList.add(HIDDEN) // hide on initial load
//      }
//    })
//    elems
//  }

  @dom def build = {

    var labels = toBindingSeq(tabLabels)
    var contents = toBindingSeq(tabContents)
    var allTabs = labels.all.bind
    var allContent = contents.all.bind
    
    <div>
      <div class={ className }>
        <ul>
          { labels.map(label => makeLabel(label, allTabs.indexOf(label)).bind) }
        </ul>
      </div>
      <div>
          { contents.map(content => makeContent(content, allContent.indexOf(content)).bind) }
      </div>
    </div>
  }
}

case class TabContentBuilder() extends ComponentBuilder{
    def render = this
    var content: Any = _
    @dom def build = {

      val inner = content match {
        case x: String => <div>{x}</div>.asInstanceOf[HTMLElement]
        case x: HTMLElement => x
        case x: Binding[Any] => <div>{x.bind.toString}</div>.asInstanceOf[HTMLElement]
        // TODO throw exception ???
      }

    inner
  }
}