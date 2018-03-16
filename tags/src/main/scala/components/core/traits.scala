package components.core

import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.NodeListOf
import components.core.Implicits._

trait Click {
  def setPointerStyle(clickHandler: Any, element: HTMLElement) = {
    Option(clickHandler).foreach(_ => element.style.cursor = "pointer")
  }
}

trait Color {
  var isPrimary: Boolean = _
  var isLink: Boolean = _
  var isInfo: Boolean = _
  var isSuccess: Boolean = _
  var isWarning: Boolean = _
  var isDanger: Boolean = _

  protected lazy val colors = Seq(isPrimary, isLink, isInfo, isSuccess, isWarning, isDanger)
  protected lazy val colorClasses = Seq(PRIMARY, LINK, INFO, SUCCESS, WARNING, DANGER)

  val INFO = "is-info"
  val LINK = "is-link"
  val PRIMARY = "is-primary"
  val DANGER = "is-danger"
  val SUCCESS = "is-success"
  val WARNING = "is-warning"

  lazy val COLOR_CLASS = colors.zip(colorClasses)
    .find({ case (color, _) => color == true })
    .map(_._2).getOrElse("")
}

//TODO default value for boolen not working...investigate
trait Size {
  var isLarge: Boolean = _
  var isMedium: Boolean = _
  var isSmall: Boolean = _

  protected lazy val sizes = Seq(isLarge, isMedium, isSmall)
  protected lazy val sizeClasses = Seq(LARGE, MEDIUM, SMALL)

  val SMALL = "is-small"
  val MEDIUM = "is-medium"
  val LARGE = "is-large"

  lazy val SIZE_CLASS = sizes.zip(sizeClasses)
    .find({ case (size, _) => size == true })
    .map(_._2).getOrElse("")
}

trait BulmaCssClasses {
  val ACTIVE = "is-active"
  val ANCESTOR = "is-ancestor"
  val BOXED = "is-boxed"
  val BUTTON = "button"
  val CARD = "card"
  val CARD_CONTENT = "card-content"
  val CENTERED = "is-centered"
  val CHILD = "is-child"
  val COLUMN = "column"
  val COLUMNS = "columns"
  val CONTAINER = "container"
  val CONTENT = "content"
  val CONTROL = "control"
  val DELETE = "delete"
  val DROPDOWN = "dropdown"
  val EXPANDED = "is-expanded"
  val HAS_ICONS_LEFT = "has-icons-left"
  val HELP = "help"
  val HORIZONTAL = "is-horizontal"
  val FIELD = "field"
  val FIXED_BOTTOM = "is-fixed-bottom"
  val FIXED_TOP = "is-fixed-top"
  val FLUID = "is-fluid"
  val FOCUSED = "is-focused"
  val FULLWIDTH = "is-fullwidth"
  val GROUPED = "is-grouped"
  val HERO = "hero"
  val HIDDEN = "is-hidden"
  val HOVERABLE = "is-hoverable"
  val ICON = "icon"
  val INVISIBLE = "is-invisible"
  val IS_ = "is-"
  val IS_UP = "is-up"
  val MEDIA = "media"
  val MEDIA_CONTENT = "media-content"
  val MEDIA_LEFT = "media-left"
  val MESSAGE = "message"
  val MESSAGE_HEADER = "message-header"
  val MESSAGE_BODY = "message-body"
  val MODAL = "modal"
  val MODAL_BUTTON = "modal-button"
  val MODAL_CLOSE = "modal-close"
  val MODAL_CONTENT = "modal-content"
  val NAVBAR = "navbar"
  val NOTIFICATION = "notification"
  val PARENT = "is-parent"
  val RIGHT = "is-right"
  val ROUNDED = "is-toggle is-toggle-rounded"
  val SELECTED = "is-selected"
  val TABLE = "table"
  val TABLE_BORDERED = "is-bordered"
  val TABLE_HOVERABLE = "is-hoverable"
  val TABLE_STRIPED = "is-striped"
  val TABLE_NARROW = "is-narrow"
  val TABS = "tabs"
  val TILE = "tile"
  val TOGGLED = "is-toggle"
  val TRANSPARENT = "is-transparent"
  val VERTICAL = "is-vertical"
  val `3/4` = "is-three-quarters"
  val `1/2` = "is-half"
  val `2/3` = "is-two-thirds"

  // Note making this private would make compilation with fastOPTJS fail
  // (maybe related to https://github.com/sbt/sbt/issues/2490 ???)
  protected def getClassToken(condition: Boolean, token: String) = if (condition) List(token) else Nil

  type CandT = Either[(Boolean, String), String]
  import scala.language.implicitConversions
  implicit def toEitherRight(s: String) = Right(s)
  implicit def toEitherLeft(ct: (Boolean, String)) = Left(ct)
  def getClassName(conditionsAndTokens: CandT*): String = {
    conditionsAndTokens.map(x => x match {
      case Left(ct) => getClassToken(ct._1, ct._2)
      case Right(t) => getClassToken(true, t)
    }).reduceLeft(_ ++ _).filter(!_.isEmpty).mkString(" ") 
    //TODO invetstigate why applying the filer before reduce doesn't work: check tile classes
  }
}

trait HTMLClassManipulator {
  def removeClassAttributeIfEmpty(elem: HTMLElement) = {
    if (elem.classList.length == 0) {
      elem.removeAttribute("class")
    }
  }
}

trait ClickableToggleWithSiblings extends BulmaCssClasses with HTMLClassManipulator {
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