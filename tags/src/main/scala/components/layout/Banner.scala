package components.layout

import components.Components.Implicits._
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLButtonElement,
  HTMLAnchorElement
}
import com.thoughtworks.binding.dom

case class BannerBuilder() extends ComponentBuilder with Color with Size {
  def render = this

  var content: HTMLElement = _ //TODO allow for more types

  @dom def build = {
    <section class={getClassName(HERO, COLOR_CLASS, SIZE_CLASS)}>
        <div class="hero-body">
            <div class="container">
                { content }
            </div>
         </div>
    </section>
  }
}
