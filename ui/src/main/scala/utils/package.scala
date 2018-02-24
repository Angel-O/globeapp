import org.scalajs.dom.console
import com.github.dwickern.macros.NameOf._
import com.github.ghik.silencer.silent

package object utils {
  val api = ApiMiddleware
  val jwt = JwtMiddleware
  val persist = PersistStateMiddleware
  val log = console
  lazy val nameOf = com.github.dwickern.macros.NameOf
  type Push = HashChanger
  type safe = com.github.ghik.silencer.silent
}
