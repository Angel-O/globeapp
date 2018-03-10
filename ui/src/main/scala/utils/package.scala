import org.scalajs.dom.console
import com.github.dwickern.macros.NameOf._
import components.core.ComponentBuilder
import components.core.Implicits._
import components.core.Helpers._
import com.thoughtworks.binding.{dom, Binding}, Binding.F
import components.core.ComponentBuilder

//import com.github.ghik.silencer.silent

package object utils {
  val api = ApiMiddleware
  val jwt = JwtMiddleware
  val persist = PersistStateMiddleware
  val log = console
  lazy val nameOf = com.github.dwickern.macros.NameOf
  type Push = HashChanger
  @dom def generateSeq[T](elements: Seq[T],
                          func: => T => Binding.F[ComponentBuilder]) = {
    toBindingSeq(elements)
      .map(element => func(element).bind)
      .all
      .bind
  }
//  type safe = com.github.ghik.silencer.silent
}
