package componentsTrial

import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var

import org.scalajs.dom.raw.{ Event, HTMLAnchorElement }

case class Button private(label: Var[String], onclick: (Var[Boolean], Var[String]) => Unit) {
  
  var clicked = Var(false)
  
  val handleOnclick = (e: Event) => onclick(clicked, label)
  
  @dom val className = s"button ${if (clicked.bind) "is-active" else "is-outlined"} is-success is-primary"
  
  @dom def render = {
    <a onclick={handleOnclick} class={className.bind}>
  			{ label.bind }
		</a>.asInstanceOf[HTMLAnchorElement]
  }
}

case object Button {
  
  def apply(label: Var[String], onclick: (Var[Boolean], Var[String]) => Unit): Binding[HTMLAnchorElement] = {
    val btn = new Button(label, onclick)
    btn.render
  }
  
  def apply(label: String, onclick: () => Unit): Binding[HTMLAnchorElement] = {
    val btn = new Button(Var(label), (_: Var[Boolean], _:Var[String]) => onclick())
    btn.render
  }
}