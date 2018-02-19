import org.scalajs.dom.console

package object utils {
  val api = ApiMiddleware
  
  implicit val log = console
}