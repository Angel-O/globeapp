import org.scalajs.dom.console
import com.github.dwickern.macros.NameOf._
import components.core.ComponentBuilder
import components.core.Implicits._
import components.core.Helpers._
import com.thoughtworks.binding.{dom, Binding}, Binding.F
import components.core.ComponentBuilder
import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import diode.NoAction
import diode.Action
import navigation.URIs.LoginPageURI

//import com.github.ghik.silencer.silent

package object utils {
  val api = ApiMiddleware
  val jwt = JwtMiddleware
  val persist = PersistStateMiddleware
  val log = console
  lazy val nameOf = com.github.dwickern.macros.NameOf
  type Push = HashChanger
  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)
  @dom def generateSeq[T](elements: Seq[T],
                          func: => T => Binding.F[ComponentBuilder]) = {
    toBindingSeq(elements)
      .map(element => func(element).bind)
      .all
      .bind
  }
  
  implicit class RedirectFuture[B <: Action](x: Future[B]) extends Push{
    def redirectToLoginOnFailure: Future[B] = {
      x.recoverWith({ case _ => push(LoginPageURI); Future{ NoAction } })
    }
  }
//  type safe = com.github.ghik.silencer.silent
}
