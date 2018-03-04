package views.catalog

import components.Components.Implicits.{CustomTags2, toHtml, toBindingSeq}
import components.table.TableRowBuilder
import com.thoughtworks.binding.{dom, Binding}, Binding.{BindingSeq, Var}
import hoc.form.LoginForm
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Login}
import org.scalajs.dom.raw.Event
import appstate.{MobileAppsSelector, FetchAllMobileApps}
import apimodels.MobileApp
import utils.generateSeq

object CatalogPage {

  def view() = new RoutingView() with MobileAppsSelector {

    dispatch(FetchAllMobileApps) // fetching on first load

    val apps: Var[Seq[MobileApp]] = Var(Seq.empty) //TODO how to use Vars??
    val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")

    @dom
    override def element = {

      //NOTE 1: calling value rather than bind on apps would cause the 
      //fetching apps on first load to fail.
      //Note 2: bind the rows before passing them to the table
      val tableRows = generateRows(apps.bind).bind 

      <div>
			  <h1>Apps catalog</h1>
        <Table 
          isBordered={true}
          isStriped={true}
          isFullWidth={true}
          isHoverable={true}
          header={<TableHeader cells={headers}/>}
          rows={tableRows}
          footer={<TableFooter cells={headers}/>}/>
      </div>
    }

    @dom
    def generateRows(apps: Seq[MobileApp]) = {
      @dom def formatPrice(price: Double) = if(price > 0) price else "FREE"
      toBindingSeq(apps) 
        .map(app =>
          <TableRow cells={Seq(app.name, app.company, app.genre, formatPrice(app.price).bind, app.store)}/>)
        .all
        .bind
    }

    def onSmartClose() = navigateTo(history.getLastVisited)

    def connectWith() = apps.value = getAllApps()
  }
}
