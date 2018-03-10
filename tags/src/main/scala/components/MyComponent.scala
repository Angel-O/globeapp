package components

import components.core.Implicits._
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.dom
import components.core.ComponentBuilder

case class MyComponentBuilder() extends ComponentBuilder {
  def render = this
  var foo: String = _
  var inner: HTMLElement = _
  @dom def build = <div>{ foo }{ inner }</div>.asInstanceOf[HTMLElement] 
}