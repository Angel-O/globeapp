package components.core

import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.Node
import org.scalajs.dom.raw.DOMList
import scala.xml.Elem
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.dom
import scala.language.implicitConversions
import com.thoughtworks.binding.Binding.Constants
import org.scalajs.dom.console
import scala.language.implicitConversions
import scala.scalajs.js.WrappedArray

object Implicits {
  implicit val log = console
  
  implicit def autoBinding[A](a: A): Binding[A] = Var(a)
  
  implicit class NodeListSeq[T <: Node](nodes: DOMList[T]) extends IndexedSeq[T] {
    override def foreach[U](f: T => U): Unit = {
      for (i <- 0 until nodes.length) {
        f(nodes(i))
      }
    }

    override def length: Int = nodes.length

    override def size: Int = length

    def count: Int = length

    override def apply(idx: Int): T = nodes(idx)
  }

  //TODO what does identity do??? commnd click on checkbox input builder ==> fieldClassName property...
  implicit def toHTMLElement(x: Elem) = identity(x.asInstanceOf[HTMLElement])

  // TODO is it a safe cast??
  implicit def fromNodeToElement(x: org.scalajs.dom.raw.Node) = identity(x.asInstanceOf[HTMLElement])

  // TODO is it a safe cast??
  implicit def fromTargetToElement(x: org.scalajs.dom.raw.EventTarget) = x.asInstanceOf[HTMLElement]

  //IF Things go wrong comment this out (to test the above...comment this out as well)
  implicit def toHTMLElementBinding(x: Elem) = Var { x.asInstanceOf[HTMLElement] }

  //DANGEROUS... turns "flatmap(_bind)" to "map(_bind)"
  // implicit def toHTMLBinding(x: ComponentBuilder) = Binding{x.asInstanceOf[HTMLElement]}
  // implicit def toSomething(x: ComponentBuilder) = x.build.bind

  //NOT USED...
  //    def getAll(selector: String): NodeList = {
  //      document.querySelectorAll(selector)
  //    }

  implicit def toComponentBuilder(x: HTMLElement): ComponentBuilder = {
    new GenricComponentBuilder(x)
  }

  implicit def toComponentBuilder(x: Elem): ComponentBuilder = ???

  implicit def toHtml(x: ComponentBuilder): BindingSeq[HTMLElement] = {
    Constants(x.build).mapBinding(identity)
  }
  
  // NOT USED
  implicit class toS(x: Any) {
    def toStr: String = x match {
      case y: WrappedArray[_] => y.array.mkString
    }
  }

  implicit class conv[T](els: BindingSeq[T]) extends IndexedSeq[T] {

    override def length: Int = 0

    override def size: Int = length

    def count: Int = length

    override def apply(idx: Int): T = els.head

    //Experimental
    def convertToSeq() = {
      var seq: Seq[T] = Seq.empty
      @dom def extract = { els.all.bind.foreach(x => seq = seq :+ x) }

      @dom def exec = { extract.bind }
      exec

      seq
    }
  }

  @dom def toScalaSeq[T](elements: BindingSeq[T]) = {
    @dom def getAll() = elements.all.bind
    getAll().bind
  }

  type GenFn = PartialFunction[Seq[Any], Any]

}