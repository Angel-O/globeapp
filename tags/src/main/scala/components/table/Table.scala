package components.table

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Helpers._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLInputElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.raw.Node
import com.thoughtworks.binding.Binding.Constants
import components.core.Click
import components.Components.Table


trait TableElementRenderer{
  @dom def renderElement(element: Any, isHeadElement: Boolean = false) = element match {
    case el: HTMLElement => if (isHeadElement) <th> {el} </th> else <td> {el} </td>
      case s: String => if (isHeadElement) <th> {s} </th> else <td> {s} </td>
      case cb: ComponentBuilder => if (isHeadElement) <th> {cb.build.bind} </th> else <td> { cb.build.bind } </td>
      case _ => if (isHeadElement) <th> {element.toString} </th> else <td>{ element.toString }</td>
  }
}
case class TableDataBuilder() extends ComponentBuilder with TableElementRenderer{
  def render = this
  var content: Binding[Any] = _
  
  @dom def build = renderElement(content.bind).bind
}

case class TableHeaderBuilder() extends ComponentBuilder {
  def render = this

  var cells: Seq[Any] = _ 

  @dom def build = {  
    
    val cells = toBindingSeq(this.cells)
    
    <thead>
      <tr>
        { cells.flatMap(x => <TableData content={ Binding{x} }/>).all.bind }
      </tr>
    </thead>.asInstanceOf[HTMLElement]
  }
}

case class TableFooterBuilder() extends ComponentBuilder {
  def render = this

  var cells: Seq[Any] = _

  @dom def build = {
    
    val cells = toBindingSeq(this.cells)
    
    <tfoot>
      <tr>
        { cells.map(x => <TableData content={ Binding{x} }/>).flatMap(_.bind) }
      </tr>
    </tfoot>.asInstanceOf[HTMLElement]
  }
}

case class TableRowBuilder() extends ComponentBuilder with TableElementRenderer with Click{
  def render = this

  var cells: Seq[Any] = _
  var onClick: Int => Unit = _ 
  var onHover: Int => Unit = _
  var onLeave: Int => Unit = _
  var index: Int = _
  var enableRowHighlight: Boolean = false
  
  private def handleClick = (e: Event) => { 
    Option(onClick).foreach( handler => handler(index))
  }

  private def handleHover = (e: Event) => { 
    Option(onHover).foreach( handler => handler(index))
  }

  private def handleLeave = (e: Event) => { 
    Option(onLeave).foreach( handler => handler(index))
  }

  private def highlightRow = (e: Event) => {
    var self = e.currentTarget.asInstanceOf[HTMLElement]
    self.classList.toggle(SELECTED)
  }

  @dom def build = {

    val tail = toBindingSeq(this.cells.tail)
    val head = this.cells.head
    val headElement = renderElement(head.bind, isHeadElement = true).bind
    val row = <tr>
                { headElement }
                { tail.map(x => <TableData content={ x }/>).flatMap(_.bind) }
              </tr>.asInstanceOf[HTMLElement]
    
    if (enableRowHighlight) row.addEventListener("click", highlightRow)

    //TODO move logic to click trait using option
    row.addEventListener("click", handleClick)
    row.addEventListener("mouseover", handleHover)
    row.addEventListener("mouseleave", handleLeave)
    
    setPointerStyle(onClick, row)
    setPointerStyle(onHover, row)

    row
  }
}

case class TableBuilder() extends ComponentBuilder {
  def render = this
  var header: TableHeaderBuilder = _
  var footer: TableFooterBuilder = _
  var rows: Seq[TableRowBuilder] = _
  var isBordered: Boolean = false
  var isStriped: Boolean = false
  var isNarrow: Boolean = false
  var isFullWidth: Boolean = false
  var isHoverable: Boolean = false
  var enableRowHighlight: Boolean = false
  var onMouseLeave: () => Unit = _
  var onMouseEnter: () => Unit = _

  private def handleLeave = (e: Event) => { 
    Option(onMouseLeave).foreach( handler => handler.apply())
  }

  private def handleEnter = (e: Event) => { 
    Option(onMouseEnter).foreach( handler => handler.apply())
  }
  
  // using lazy val rather than val...
  private lazy val className = getClassName(
      TABLE,
      (isBordered, TABLE_BORDERED),
      (isStriped, TABLE_STRIPED),
      (isNarrow, TABLE_NARROW),
      (isFullWidth, FULLWIDTH),
      (isHoverable, TABLE_HOVERABLE))

  @dom def build = {
    
    val rowsAndIndices = toBindingSeq(rows.zipWithIndex)
    
    val table = 
      <table class={className}>
        { unwrapBuilder(header) }
        <tbody>
          { rowsAndIndices.flatMap(rowAndIndex => getIndexedRowAndAddProperties(rowAndIndex).bind) }
        </tbody>
        { unwrapBuilder(footer) }
      </table>

    table.addEventListener("mouseleave", handleLeave)
    table.addEventListener("mouseenter", handleEnter)

    table
  }

  @dom def getIndexedRowAndAddProperties(rowAndIndex: (TableRowBuilder, Int)) = {
    rowAndIndex._1.index = rowAndIndex._2 // add index
    rowAndIndex._1.enableRowHighlight = enableRowHighlight // set row highlight
    rowAndIndex._1.bind
  }
}