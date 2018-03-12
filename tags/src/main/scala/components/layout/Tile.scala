package components.layout

import org.scalajs.dom.raw.HTMLElement
import components.core.Implicits._
import components.core.Helpers._
import com.thoughtworks.binding.dom
import org.scalajs.dom.raw.Event
import components.core.ComponentBuilder
import components.core.Color
import components.core.Click

case class TileBuilder() extends ComponentBuilder with Color with Click{
  def render = this
  
  var isAncestor: Boolean = false
  var isParent: Boolean = _
  var isVertical: Boolean = _ //TODO can a child be vertical???
  var width: Int = _ //TODO throw exception if it's out of range
  var children: Seq[TileBuilder] = Seq.empty
  var content: HTMLElement = _ // TODO allow for strings as well...
  var onClick, onHover: () => Unit = _
   
  private var isChild: Boolean = _ //NO NEED TO Set it from outside!!
  
  private lazy val onClickOption = Option(onClick)
  private lazy val onHoverOption = Option(onHover)
  
  lazy val hasWidth = width > 0
  lazy val className = getClassName(
        TILE, COLOR_CLASS, 
        (!COLOR_CLASS.isEmpty, NOTIFICATION), 
        (isAncestor, ANCESTOR),
        (isParent, PARENT),
        (isChild, CHILD),
        (isVertical, VERTICAL), 
        (hasWidth, s"$IS_$width"))
  
  @dom private def element = {

    // tiles can have 1 contextual token at most
    val contextualTokens =
      Seq(isAncestor, isParent, isChild).foldLeft(0)((totalTokens, token) =>
        if (token) totalTokens + 1 else totalTokens)

    if (contextualTokens > 1) {
      throw new IllegalArgumentException(
        "Tiles can be either ancestor, parent, child or have no contextual tokens associated to them. " +
        s"Found multuple tokens: ($className)")
    }

    // a tile will either have a content or sub-tiles (aka children).
    // If the content is to be rendered rather than the children tiles
    // it needs to be turned into a component builder, then wrapped into a Seq.
    // This will allow the tileContent type to be Seq[ContentBuilder], that can be
    // can be easily rendered with a flatMap atfer being wrapped into a bindingSeq
    var tileContent = if (content == null) children else Seq(toComponentBuilder(content))
    
    val childrenTiles = toBindingSeq(tileContent)

    //TODO allow for articles, not just divs...
    val elem = 
      <div class={className}>
        { childrenTiles.flatMap(_.bind) }
    	</div>.asInstanceOf[HTMLElement]
    
    //TODO map over option to avoid attaching event handlers that do nothing (DONE)
    //and move this logic to click Trait (TO BE DONE)
    onClickOption.map(handler => elem.addEventListener("click", (e: Event) => handler()))
    onHoverOption.map(handler => elem.addEventListener("mouseenter", (e: Event) => handler())) 
    
    setPointerStyle(onClick, elem)
    
    elem
  }
  
  @dom def build = {
    
    if(!isChild && content != null){
      throw new IllegalArgumentException("Only children tiles can have content")
    }
    
    if(!isAncestor && isParent){ 
      children.foreach(_.isChild = true)   
    }

    element.bind
  }
}