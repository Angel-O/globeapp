package components.icon

import components.core.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLButtonElement, HTMLAnchorElement }
import com.thoughtworks.binding.dom
import components.core.ComponentBuilder

case class IconBuilder() extends ComponentBuilder{
    def render = this
    var id: String = _
    @dom def build = {
        require(id != null, "icon id cannot be null")
        <i class={s"fa fa-$id"}/>
    }
}