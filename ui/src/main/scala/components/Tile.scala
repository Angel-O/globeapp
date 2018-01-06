package components

import org.scalajs.dom.raw.HTMLElement

import com.thoughtworks.binding.dom

import Components.Implicits.ComponentBuilder
import Components.Implicits.autoBinding
import Components.Implicits.toBindingSeq
import Components.Implicits.toComponentBuilder

case class TileBuilder() extends ComponentBuilder{
  def render = this
  
  var isAncestor: Boolean = false
  var isParent: Boolean = _
  var isChild: Boolean = _ //NO NEED TO Set it!!
  var isVertical: Boolean = _ //TODO can a child be vertical???
  var size: Int = _
  var children: Seq[TileBuilder] = Seq.empty
  var content: HTMLElement = _ // TODO allow for strings as welll...
  
  lazy val hasSize = size > 0
  lazy val className = getClassName(
        (true, TILE), 
        (isAncestor, ANCESTOR),
        (isParent, PARENT),
        (isChild, CHILD),
        (isVertical, VERTICAL), 
        (hasSize, s"$IS_$size"))
  
  @dom private def element = {
    // a tile will either have a content or sub-tiles (aka children).
    // If the content is to be rendered rather than the children tiles
    // it needs to be turned into a component builder, then wrapped into a Seq.
    // This will allow the tileCOntent type to be Seq[ContentBuilder], that can be
    // can be easily rendered with a flatMap atfer being wrapped into a bindingSeq
    var tileContent = if (content == null) children else Seq(toComponentBuilder(content))
    
    val childrenTiles = toBindingSeq(tileContent)    
    
    //TODO allow for articles, not just divs...
    <div class={className}>
      { childrenTiles.flatMap(_.bind) }
    </div>.asInstanceOf[HTMLElement]
  }
  
  @dom def build = {
    
    if(!isAncestor){
      if(isParent){
        children.foreach(x => x.isChild == true)
      }      
    }
    element.bind
  }
}