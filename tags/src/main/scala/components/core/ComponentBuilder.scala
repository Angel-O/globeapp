package components.core

import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding._, Binding._
import components.core.Implicits._
import components.core.Helpers._
import org.scalajs.dom.document
import org.scalajs.dom.raw.Node

abstract class ComponentBuilder extends BulmaCssClasses { //with Dynamic{
  //abstract class ComponentBuilder extends BulmaCssClasses {

  def render: ComponentBuilder
  def build: Binding[HTMLElement]

  // lazy val avoids compiler issues if using xml syntax <Dummy/>...it doesn't completely work
  // but there is no time to invstigate
  protected lazy val dummy: ComponentBuilder = DummyBuilder.asInstanceOf[ComponentBuilder]
  def unwrapBuilder(optional: => ComponentBuilder, condition: Boolean = true): ComponentBuilder = {
    if (condition) Option(optional).fold(dummy)(identity)
    else dummy
  }

  // translated: if the condition is true use the element, if not use a placeholder
  @dom def unwrapElement(optional: => HTMLElement, condition: Boolean = true) = {
    if (condition) Option(optional).fold(dummy.build)(opt => Binding { opt })
    else dummy.build
  }.bind

  def listen = {
    def create = toBindingSeq(Seq(build))
    val elem = create.map(_.bind)
    elem.map(_.asInstanceOf[HTMLElement])
    //this is equivalent to ----> toHtml(this)
  }

  // create custom tags visible on dev tools
  def create(content: Node, tagName: String) = {

    //TODO create custom class with "display = block" and other styles (or use Bulma equivalent)
    // rather than assigning the property
    val element = document.createElement(tagName)
    (new dom.Runtime.NodeSeqMountPoint(element, content)).watch()
    element.asInstanceOf[HTMLElement].style.display = "block"
    element.asInstanceOf[HTMLElement]
  }

  //TODO remove all dynamic stuff or create a dynamicComponentBuilder
  //      import scala.reflect.ClassTag
  //
  //      def toGenFn0(f: => Any): GenFn = { case Seq() => f; }
  //      def toGenFn1[A: ClassTag](f: (A) => Any): GenFn = { case Seq(x1: A) => f(x1); }
  //      def toGenFn2[A: ClassTag, B: ClassTag](f: (A, B) => Any): GenFn = { case Seq(x1: A, x2: B) => f(x1, x2); }
  //
  //      //TODO ablity to add dynamic function fields with more than 0 params and Any other type...
  //      //private val fields = mutable.Map.empty[String, Any].withDefault {key => null}
  //      private val fields = mutable.Map.empty[String, Function1[Any, Any]].withDefault {key => null}
  //
  //      def selectDynamic(key: String) = fields(key)
  //
  //      //def updateDynamic(key: String)(value: Function0[_]) = fields(key) = value
  //      def updateDynamic(key: String)(value: Function1[Any, Any]) = fields(key) = value
  //      //def updateDynamic(key: String)(value: () => Any) = fields(key) = value
  //      //def updateDynamic(key: String)(value: (Any*) => Any) = fields(key) = value
  //      //def updateDynamic(key: String)(value: Function1[Any, Any]) = fields(key) = value
  ////      def updateDynamic(key: String)(value: GenFn) =
  ////        fields.get(key) match {
  ////          case None     => fields(key) = value
  ////          case Some(f)  => fields(key) = f.orElse(value);
  ////        }
  //
  //      def applyDynamic(key: String)(args: Any*) = {//(implicit tag: TypeTag[T]) = { //args prolly not needed... just return the function
  //        fields(key) match {
  //          //case f: Function0[_] => f() // executes a Zero arg function
  //          //case f: ((Any) => Any) => ??? //if (tag.tpe =:= typeOf[Any]) f(args)
  //          case f: Function1[Any, Any] => f(args) //TODO find a way to pass args
  //          //case f: GenFn => f(args)
  ////          case i: Int => i // primitive types
  ////          case c: Char => c
  ////          case b: Boolean => b
  ////          case s: String => s
  ////          case _ => fields(key) //reference types
  //        }
  //      }
}

case class GenricComponentBuilder(element: HTMLElement) extends ComponentBuilder {
  def render = this
  @dom def build = <div>{ element }</div>.asInstanceOf[HTMLElement]
}

case object DummyBuilder extends ComponentBuilder {
  def render = this
  @dom def build = <div class="dummy"><!-- --></div>.asInstanceOf[HTMLElement]
}