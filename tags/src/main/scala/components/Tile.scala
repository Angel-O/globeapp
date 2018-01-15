package components

import org.scalajs.dom.raw.HTMLElement

import com.thoughtworks.binding.dom

import Components.Implicits.ComponentBuilder
import Components.Implicits.autoBinding
import Components.Implicits.toBindingSeq
import Components.Implicits.toComponentBuilder
import components.Components.Implicits.Color
import org.scalajs.dom.raw.Event

case class TileBuilder() extends ComponentBuilder with Color{
  def render = this
  
  private val defaultHandler : () => Unit = () => {}
  
  var isAncestor: Boolean = false
  var isParent: Boolean = _
  var isVertical: Boolean = _ //TODO can a child be vertical???
  var width: Int = _ //TODO throw exception if it's out of range
  var children: Seq[TileBuilder] = Seq.empty
  var content: HTMLElement = _ // TODO allow for strings as well...
  var onClick, onHover: () => Unit = defaultHandler //do nothing by default
   
  private var isChild: Boolean = _ //NO NEED TO Set it from outside!!
  
  private val handleOnClick = (e: Event) => onClick()
  private val handleOnHover = (e: Event) => onHover()
  
  lazy val hasWidth = width > 0
  lazy val className = getClassName(
        (true, TILE), 
        (isAncestor, ANCESTOR),
        (isParent, PARENT),
        (isChild, CHILD),
        (isVertical, VERTICAL), 
        
        // TODO allow only one notification modifier or set priority...this logic could be handled in the 
        // Color trait...
        (isPrimary, getClassName((true, NOTIFICATION), (true, PRIMARY))),
        (isWarning, getClassName((true, NOTIFICATION), (true, WARNING))),
        (isInfo, getClassName((true, NOTIFICATION), (true, INFO))),
        (isSuccess, getClassName((true, NOTIFICATION), (true, SUCCESS))),
        (isDanger, getClassName((true, NOTIFICATION), (true, DANGER))),
        (hasWidth, s"$IS_$width"))
  
  @dom private def element = {
    // a tile will either have a content or sub-tiles (aka children).
    // If the content is to be rendered rather than the children tiles
    // it needs to be turned into a component builder, then wrapped into a Seq.
    // This will allow the tileCOntent type to be Seq[ContentBuilder], that can be
    // can be easily rendered with a flatMap atfer being wrapped into a bindingSeq
    var tileContent = if (content == null) children else Seq(toComponentBuilder(content))
    
    val childrenTiles = toBindingSeq(tileContent) 
    
    var contextualTokens = 0
    if (className.contains(ANCESTOR)) contextualTokens += 1
    if (className.contains(PARENT)) contextualTokens += 1
    if (className.contains(CHILD)) contextualTokens += 1
        
    if (contextualTokens > 1){
      throw new IllegalArgumentException(s"Tiles can be either ancestor, parent or child")
    }
    
    //TODO allow for articles, not just divs...
    val elem = 
      <div class={className}>
        { childrenTiles.flatMap(_.bind) }
    	</div>.asInstanceOf[HTMLElement]
    
    elem.addEventListener("click", handleOnClick)
    elem.addEventListener("mouseenter", handleOnHover)
    
    if (onClick != defaultHandler){
      elem.style.cursor = "pointer"
    }
    
    elem
  }
  
  @dom def build = {
    
    if(!isChild && content != null){
      throw new IllegalArgumentException("Only children tiles can have content")
    }
    
    if(!isAncestor){
      if(isParent){
        children.foreach(x => x.isChild = true)
      }      
    }
    element.bind
  }
}