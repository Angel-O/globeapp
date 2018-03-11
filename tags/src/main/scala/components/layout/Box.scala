package components.layout

import components.core.Implicits._
import components.core.Helpers._
import org.scalajs.dom.raw.{Event, HTMLElement}
import com.thoughtworks.binding.dom
import components.core.ComponentBuilder
import components.core.Color
import components.core.Size

case class BoxBuilder() extends ComponentBuilder with Color {
  def render = this

  var sizes: Seq[String] = _
  var contents: Seq[HTMLElement] = _ //TODO allow for more types

  @dom def build = {

    val element =
      <div class={getClassName(COLUMNS)}>     
            { toBindingSeq(contents.zipWithIndex)
                .map( { case(html, i) => 
                    <div class={getClassName(
                        COLUMN, 
                        if(i < sizes.size) sizes(i) else "")}> 
                        { html } 
                    </div> } )
                .all.bind }    
      </div>

    element
  }
}
