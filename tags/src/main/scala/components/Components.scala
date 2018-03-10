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
import components.button.{ SimpleButtonBuilder, ButtonBuilder, ButtonBuilderRaw }
import components.table._
import components.dropdown._
import components.modal._
import components.input._
import components.icon._
import components.layout._
import components._

import scala.xml.Elem
import scala.xml.UnprefixedAttribute

import router.BrowserRouterBuilder
import router.RouteBuilder
import components.core.DummyBuilder
import components.core.ComponentBuilder
import components.core.GenricComponentBuilder

//import scala.reflect.api.TypeTags
//import scala.reflect.runtime.universe._
object Components {

  import scala.language.dynamics
  import scala.collection.mutable
  implicit final class CustomTags2(x: dom.Runtime.TagsAndTags2.type) extends Dynamic {

    type BuilderFunction = Function[Seq[Any], ComponentBuilder]

    private val fields = mutable.Map.empty[String, BuilderFunction].withDefault { key => throw new NoSuchFieldError(key) }

    def selectDynamic(key: String) = fields(key)

    def updateDynamic(key: String)(value: BuilderFunction) = fields(key) = value

    def applyDynamic(key: String)(args: BuilderFunction*) = fields(key)

    def RegisterTag(builder: BuilderFunction, tagName: String) = this.tagName = builder
  }

  implicit final class Button(x: dom.Runtime.TagsAndTags2.type) {
    def Button() = new ButtonBuilder()
    def ButtonRaw() = new ButtonBuilderRaw()
    def SimpleButton() = new SimpleButtonBuilder()
  }

  implicit final class Card(x: dom.Runtime.TagsAndTags2.type) {
    def Card() = new CardBuilder()
    def CardImage() = new CardImageBuilder()
  }

  implicit final class Dropdown(x: dom.Runtime.TagsAndTags2.type) {
    def Dropdown() = new DropdownBuilder()
    def MenuItem() = new MenuItemBuilder()
  }

  implicit final class Input(x: dom.Runtime.TagsAndTags2.type) {
    def CheckboxInput() = new CheckboxInputBuilder()
    def EmailInput() = new EmailInputBuilder()
    def FieldValidation() = new FieldValidationBuilder()
    def Input() = new InputBuilder()
    def InputRaw() = new InputBuilderRaw()
    def PasswordInput = new PasswordInputBuilder()
    def RadioInput = new RadioInputBuilder()
    def SelectInput = new SelectInputBuilder()
    def TextareaInput() = new TextareaInputBuilder()
    def TextInput() = new TextInputBuilder()
  }

  implicit final class Layout(x: dom.Runtime.TagsAndTags2.type) {
    def Banner() = new BannerBuilder()
    def Message() = new MessageBuilder()
    def Tile() = new TileBuilder()
  }

  implicit final class Modal(x: dom.Runtime.TagsAndTags2.type) {
    def ModalCard() = new ModalCardBuilder()
    def SimpleModal() = new SimpleModalBuilder()
  }

  implicit final class Router(x: dom.Runtime.TagsAndTags2.type) {
    def BrowserRouter() = new BrowserRouterBuilder()
    def Route() = new RouteBuilder()
  }

  implicit final class Misc(x: dom.Runtime.TagsAndTags2.type) {
    def Icon() = new IconBuilder()
    def MyComponent() = new MyComponentBuilder()
  }

  implicit final class Navbar(x: dom.Runtime.TagsAndTags2.type) {
    def Navbar() = new NavbarBuilder()
    def NavbarItem() = new NavbarItemBuilder()
    def NavbarLogo() = new NavbarLogoBuilder()
  }

  implicit final class Tab(x: dom.Runtime.TagsAndTags2.type) {
    def TabContent() = new TabContentBuilder()
    def TabSwitch() = new TabSwitchBuilder()
  }

  implicit final class Table(x: dom.Runtime.TagsAndTags2.type) {
    def Table() = new TableBuilder()
    def TableData() = new TableDataBuilder()
    def TableHeader() = new TableHeaderBuilder()
    def TableFooter() = new TableFooterBuilder()
    def TableRow() = new TableRowBuilder()
  }

  implicit final class Util(x: dom.Runtime.TagsAndTags2.type) {
    def Dummy() = DummyBuilder
    def Wrapper(x: HTMLElement) = new GenricComponentBuilder(x)
  }
}
