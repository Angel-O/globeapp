package macros

import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding._, Binding._
//import com.thoughtworks.binding.Binding
//import com.thoughtworks.binding.Binding.Var
//import com.thoughtworks.binding.Binding.Vars
//import com.thoughtworks.binding.Binding.F
//import scala.scalajs.js
//import org.scalajs.dom.html.Div
//import scala.xml.{Elem, MetaData, NamespaceBinding, Node => XmlNode, UnprefixedAttribute, NodeSeq, TopScope}
//import xml._

import scala.language.implicitConversions
import org.scalajs.dom.raw.Node
import org.scalajs.dom.raw.DOMList
import org.scalajs.dom.raw.NodeList
import org.scalajs.dom.raw.NodeListOf
import org.scalajs.dom.raw.Event


import scala.xml.Elem
import scala.xml.UnprefixedAttribute

import scala.language.dynamics
import scala.collection.mutable

import macros.RegisterTag._

object Components {

  object Implicits {

    implicit def autoBinding[A](a: A): Binding[A] = Var(a)
    
//    implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???
//    
//    implicit class Have(a: String) {
//      def create(key:String, value:Seq[String], next:xml.MetaData): UnprefixedAttribute = ???
//    }
//    object BetterAttr{
//      implicit val key:String = ""
//      implicit val value: Seq[String] = Seq.empty
//      implicit var next:xml.MetaData = _
//    }
//    class BetterAttr(implicit key:String, value: Seq[String], next:xml.MetaData) 
//    extends UnprefixedAttribute(key, key, null){
//      implicit val ke: String = ""
//      implicit val valu: Seq[String] = Seq.empty
//      implicit var nex: xml.MetaData = _
//      def create(key:String, value: Seq[String], next:xml.MetaData): UnprefixedAttribute = new BetterAttr()
//    }
//    implicit def create(implicit key:String, value: Seq[String], next:xml.MetaData): UnprefixedAttribute = 
//      new BetterAttr()

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
    
    implicit def toHTMLElement(x: Elem) = x.asInstanceOf[HTMLElement]

    //NOT USED...
    def getAll(selector: String): NodeList = {
      document.querySelectorAll(selector)
    }

    implicit final class CustomTags2(x: dom.Runtime.TagsAndTags2.type) extends Dynamic{
      
      def Dummy() = DummyBuilder
         
      // util
      def Wrapper(x: HTMLElement) = new GenricComponentBuilder(x)
      
      
      type BuilderFunction = Function[Seq[Any], ComponentBuilder]
      
      private val fields = mutable.Map.empty[String, BuilderFunction].withDefault {key => throw new NoSuchFieldError(key)}

      def selectDynamic(key: String) = fields(key)

      def updateDynamic(key: String)(value: BuilderFunction) = fields(key) = value

      def applyDynamic(key: String)(args: BuilderFunction*) = fields(key)
      
      def RegisterTag(builder: BuilderFunction, tagName: String) = {
        this.tagName = builder
      }
    }
    
    implicit def toComponentBuilder(x: HTMLElement):ComponentBuilder = {
        new GenricComponentBuilder(x)
    }
    
    implicit def toComponentBuilder(x: Elem):ComponentBuilder = ???
    
    implicit def toHtml(x: ComponentBuilder): BindingSeq[HTMLElement] = {
      //Constants(x.build).mapBinding(x => {@dom val bound = {x.bind}; bound})
      //Constants(x.build).mapBinding(x => Binding{x.asInstanceOf[HTMLElement]})
      
//      def toBindingSeq[T](elements: Seq[T]) = {
//        var temp: Vars[T] = Vars.empty; 
//        elements.foreach(x => temp.value += x)
//        var bindingElementsSeq: BindingSeq[T] = temp
//        bindingElementsSeq
//      }
      
//      x.build match{
//        case el: HTMLElement => Constants(x.build).mapBinding(identity)
//        case els: Seq[_] => toBindingSeq[Any](els).mapBinding(x => x.asInstanceOf[HTMLElement])
//        case _ => throw new IllegalArgumentException("nope!!")
//      }
//      @dom def create(content: BindingSeq[Node]) = {
//         val element = document.createElement("MyComponent")
//         (new dom.Runtime.NodeSeqMountPoint(element, content)).bind
//         val el = element.asInstanceOf[HTMLElement]
//         Constants(el).mapBinding(x => Binding{x})
//      }
//      
//      create(x)
      
      Constants(x.build).mapBinding(identity)
    }
    
//    @dom implicit def toBuilder(elem: Binding[HTMLElement]) = {
//      elem match {
//        case x: ComponentBuilder => x.build.bind
//        case _ => elem
//      }
//    }
    
//    implicit def register(tagName: String, tag: ComponentBuilder, params: Seq[Any] = Seq.empty) = 
//        CustomTags2(dom.Runtime.TagsAndTags2).RegisterTag(params => tag, tagName)
    
    def toBindingSeq[T](elements: Seq[T]) = {
        var temp: Vars[T] = Vars.empty; 
        elements.foreach(x => temp.value += x)
        var bindingElementsSeq: BindingSeq[T] = temp
        bindingElementsSeq
    }
      
    @dom def toScalaSeq[T](elements: BindingSeq[T]) = {
        @dom def getAll() = elements.all.bind
        getAll().bind
    }

    abstract class ComponentBuilder extends BulmaCssClasses {
      def render: ComponentBuilder
      def build: Binding[HTMLElement] 
      //def self: HTMLElement
      // lazy val avoids compiler issues if using xml syntax <Dummy/>...it doesn't completely work
      // but there is no time to invstigate
      protected lazy val dummy: ComponentBuilder = DummyBuilder.asInstanceOf[ComponentBuilder]
      def unwrapBuilder(optional: => ComponentBuilder, condition: Boolean = true): ComponentBuilder = {
        if(condition) Option(optional).fold(dummy)(identity)
        else dummy
      }
      @dom def unwrapElement(optional: => HTMLElement, condition: Boolean = true) = {
        if(condition) Option(optional).fold(dummy.build)(opt => Binding { opt })
        else dummy.build
      }.bind
      
      def getClassToken(condition: Boolean, token: String) = if (condition) List(token) else Nil

      def getClassName(conditionsAndTokens: (Boolean, String)*) = {
        conditionsAndTokens.map(x => getClassToken(x._1, x._2)).reduceLeft(_ ++ _).mkString(" ")
      }
      
      def listen = {
        def create = toBindingSeq(Seq(build))
        val elem = create.map(_.bind)
        elem.map(_.asInstanceOf[HTMLElement])
        //this is equivalent to ----> toHtml(this)
      }
      
      def register(tagName: String, tag: ComponentBuilder, params: Seq[Any] = Seq.empty) = 
        CustomTags2(dom.Runtime.TagsAndTags2).RegisterTag(params => tag, tagName)
    }

    case class GenricComponentBuilder(element: HTMLElement) extends ComponentBuilder {
      def render = this
      @dom def build = <div>{ element }</div>.asInstanceOf[HTMLElement] 
    }
    
    //case class MyComponentBuilder(content: BindingSeq[Node] = DummyBuilder) extends ComponentBuilder {
    case class MyComponentBuilder() extends ComponentBuilder {
      def render = this
      var foo: String = _
      var inner: HTMLElement = _
      @dom def build = <div>{ foo.bind }{ inner.bind }</div>.asInstanceOf[HTMLElement] // create(content).bind

      //      @dom def create(content: BindingSeq[Node]) = {
      //        val element = document.createElement("MyComponent")
      //        (new dom.Runtime.NodeSeqMountPoint(element, content)).bind
      //        element.asInstanceOf[HTMLElement]
      //      }
    }

    case object DummyBuilder extends ComponentBuilder {
      def render = this
      @dom def build = <div class="dummy"><!-- --></div>.asInstanceOf[HTMLElement]
    }
    
    trait BulmaCssClasses {
      val ACTIVE = "is-active"
      val SELECTED = "is-selected"
      val FOCUSED = "is-focused"
      val PRIMARY = "is-primary"
      val BUTTON = "button"
      val TABLE = "table"
      val TABLE_BORDERED = "is-bordered"
      val TABLE_STRIPED = "is-striped"
      val TABLE_NARROW = "is-narrow"
      val TABLE_HOVERABLE = "is-hoverable"
      val FULLWIDTH = "is-fullwidth"
      val TABS = "tabs"
      val CENTERED = "is-centered"
      val TOGGLED = "is-toggle"
      val RIGHT = "is-right"
      val BOXED = "is-boxed"
      val ROUNDED = "is-toggle is-toggle-rounded"
      val MEDIUM = "is-medium"
      val LARGE = "is-large"
      val SMALL = "is-small"
      val INVISIBLE = "is-invisible"
      val HIDDEN = "is-hidden"
      val ICON = "icon"
      val HOVERABLE = "is-hoverable"
      val FIXED_TOP = "is-fixed-top"
      val FIXED_BOTTOM = "is-fixed-bottom"
      val NAVBAR = "navbar"
      val TRANSPARENT = "is-transparent"
      val EXPANDED = "is-expanded"
      val DROPDOWN = "dropdown"
      val IS_UP = "is-up"
      val DANGER = "is-danger"
      val SUCCESS = "is-success"
      val HELP = "help"
      val HAS_ICONS_LEFT = "has-icons-left"
      val FIELD = "field"
      val CONTROL = "control"
    }
  }
}
