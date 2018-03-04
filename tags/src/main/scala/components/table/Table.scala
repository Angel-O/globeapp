package components.table

import components.Components.Implicits._
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLInputElement }
import com.thoughtworks.binding.dom
import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.Binding.BindingSeq
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.raw.Node
import com.thoughtworks.binding.Binding.Constants

case class TableDataBuilder() extends ComponentBuilder {
  def render = this
  var content: Binding[Any] = _
  @dom def build = <td>{ content.bind.toString() }</td>.asInstanceOf[HTMLElement]
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

case class TableRowBuilder() extends ComponentBuilder {
  def render = this

  var cells: Seq[Any] = _
  var onClick: Int => Unit = (_: Int) => Unit //TODO add clickable trait
  var index: Int = _
  
  private def handleClick = (e: Event) => {
    var self = e.currentTarget.asInstanceOf[HTMLElement]
    self.classList.toggle(SELECTED)
    onClick(index)
  }

  @dom def build = {

    val tail = toBindingSeq(this.cells.tail)
    val head = this.cells.head
    val row = <tr>
                <th>{ head.toString }</th>
                { tail.map(x => <TableData content={ x }/>).flatMap(_.bind) }
              </tr>.asInstanceOf[HTMLElement]
    
    row.addEventListener("click", handleClick)
    
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
    
    <table class={className}>
      { unwrapBuilder(header) }
      <tbody>
        { rowsAndIndices.flatMap(rowAndIndex => getIndexedRow(rowAndIndex).bind) }
      </tbody>
      { unwrapBuilder(footer) }
    </table>.asInstanceOf[HTMLElement]
  }

  @dom def getIndexedRow(rowAndIndex: (TableRowBuilder, Int)) = {
    rowAndIndex._1.index = rowAndIndex._2 
    rowAndIndex._1.bind
  }
}