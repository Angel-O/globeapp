package components

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
import components.button.{SimpleButtonBuilder, ButtonBuilder, ButtonBuilderRaw}
import components.table._
import components.dropdown._
import components.modal._
import components.form._ //TODO rename this to input field

import scala.xml.Elem
import scala.xml.UnprefixedAttribute

import router.RouteBuilder
import router.BrowserRouterBuilder

object Components {

  object Implicits {

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
    
    implicit def toHTMLElement(x: Elem) = x.asInstanceOf[HTMLElement]

    //NOT USED...
    def getAll(selector: String): NodeList = {
      document.querySelectorAll(selector)
    }

    import scala.language.dynamics
    import scala.collection.mutable
    implicit final class CustomTags2(x: dom.Runtime.TagsAndTags2.type) extends Dynamic {
      
      type BuilderFunction = Function[Seq[Any], ComponentBuilder]
      
      private val fields = mutable.Map.empty[String, BuilderFunction].withDefault {key => throw new NoSuchFieldError(key)}

      def selectDynamic(key: String) = fields(key)

      def updateDynamic(key: String)(value: BuilderFunction) = fields(key) = value

      def applyDynamic(key: String)(args: BuilderFunction*) = fields(key)
      
      def RegisterTag(builder: BuilderFunction, tagName: String) = this.tagName = builder
      
      // base components
      def Button() = new ButtonBuilder()
      def ButtonRaw() = new ButtonBuilderRaw()
      def Card() = new CardBuilder()
      def CardImage() = new CardImageBuilder()
      def CheckboxInput() = new CheckboxInputBuilder()
      def Dummy() = DummyBuilder
      def Dropdown() = new DropdownBuilder()
      def EmailInput() = new EmailInputBuilder()
      def FieldValidation() = new FieldValidationBuilder()
      def Input() = new InputBuilder()
      def InputRaw() = new InputBuilderRaw()
      def MenuItem() = new MenuItemBuilder()
      //def MyComponent() = new MyComponentBuilder() // TEST
      def ModalCard() = new ModalCardBuilder()
      def Navbar() = new NavbarBuilder()
      def NavbarItem() = new NavbarItemBuilder()
      def NavbarLogo() = new NavbarLogoBuilder()
      def PasswordInput = new PasswordInputBuilder()
      def RadioInput = new RadioInputBuilder()
      def SelectInput = new SelectInputBuilder()
      def SimpleButton() = new SimpleButtonBuilder()
      def SimpleModal() = new SimpleModalBuilder()
      def Table() = new TableBuilder()
      def TableData() = new TableDataBuilder()
      def TableHeader() = new TableHeaderBuilder()
      def TableFooter() = new TableFooterBuilder()
      def TableRow() = new TableRowBuilder()
      def TabSwitch() = new TabSwitchBuilder()
      def TabContent() = new TabContentBuilder()
      def TextareaInput() = new TextareaInputBuilder()
      def TextInput() = new TextInputBuilder()  
      def Tile() = new TileBuilder()
           
      // util
      def Wrapper(x: HTMLElement) = new GenricComponentBuilder(x)
      
      // routing
      def BrowserRouter() = new BrowserRouterBuilder()
      def Route() = new RouteBuilder()
    }
    
    implicit def toComponentBuilder(x: HTMLElement):ComponentBuilder = {
        new GenricComponentBuilder(x)
    }
    
    implicit def toComponentBuilder(x: Elem):ComponentBuilder = ???
    
    implicit def toHtml(x: ComponentBuilder): BindingSeq[HTMLElement] = {     
      Constants(x.build).mapBinding(identity)
    }
    
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
      
      // create custom tags visible on dev tools
      @dom def create(content: Node, tagName: String) = {
        val element = document.createElement(tagName)
        (new dom.Runtime.NodeSeqMountPoint(element, content)).watch()
        element.asInstanceOf[HTMLElement].style.display = "block"
        element
     }
    }
    
    trait Color {
      var isPrimary: Boolean = _
      var isLink: Boolean = _
      var isInfo: Boolean = _
      var isSuccess: Boolean = _
      var isWarning: Boolean = _
      var isDanger: Boolean = _
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
      val PARENT = "is-parent"
      val CHILD = "is-child"
      val TILE = "tile"
      val VERTICAL = "is-vertical"
      val ANCESTOR = "is-ancestor"
      val WARNING = "is-warning"
      val IS_ = "is-"
      val NOTIFICATION = "notification"
      val INFO = "is-info"
    }
    
    trait HTMLClassManipulator{
      def removeClassAttributeIfEmpty(elem: HTMLElement) = {
        if(elem.classList.length == 0) {
            elem.removeAttribute("class") 
        }
      }
    }
    
    trait Size extends BulmaCssClasses{
      var isLarge: Boolean = _ 
      var isMedium: Boolean = _ 
      var isSmall: Boolean = _ 
      
      // the largest size takes precedence
      lazy val sizeIsSet = Seq(isLarge, isMedium, isSmall).find(identity).getOrElse(false)
      
      // the largest size takes precedence
      lazy val SIZE_CLASS = if(isLarge) LARGE else if(isMedium) MEDIUM else SMALL 
    }

    trait ClickableToggleWithSiblings extends BulmaCssClasses with HTMLClassManipulator{
      val toggleItem = (e: Event, classToken: String) => {
        val self = e.currentTarget.asInstanceOf[HTMLElement]
        self.classList.toggle(classToken)
        removeClassAttributeIfEmpty(self)
      }

      val deactivateSiblings = (e: Event, classToken: String) => {
        val self = e.currentTarget.asInstanceOf[HTMLElement]
        val parent = self.parentElement
        val allChildren = parent.children.asInstanceOf[NodeListOf[HTMLElement]]
        val siblings = allChildren.filter(_.classList.contains(classToken))
        siblings.foreach(_.classList.remove(classToken))
        self.classList.toggle(classToken)
        allChildren.foreach(removeClassAttributeIfEmpty _)
      }
    }

    case class GenricComponentBuilder(element: HTMLElement) extends ComponentBuilder {
      def render = this
      @dom def build = <div>{ element }</div>.asInstanceOf[HTMLElement] 
    }
    
    case object DummyBuilder extends ComponentBuilder {
      def render = this
      @dom def build = <div class="dummy"><!-- --></div>.asInstanceOf[HTMLElement]
    }
    
    case class MyComponentBuilder() extends ComponentBuilder {
      def render = this
      var foo: String = _
      var inner: HTMLElement = _
      @dom def build = <div>{ foo.bind }{ inner.bind }</div>.asInstanceOf[HTMLElement] 
    }
  }
}