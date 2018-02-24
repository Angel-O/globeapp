import org.scalajs.dom.console
import com.github.dwickern.macros.NameOf._

package object utils {
  val api = ApiMiddleware
  val jwt = JwtMiddleware
  val log = console
  lazy val nameOf = com.github.dwickern.macros.NameOf
  type Push = HashChanger
}
