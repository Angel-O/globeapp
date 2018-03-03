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

    dispatch(FetchAllMobileApps) // fetch on load...not working: issue due to routing: It's time to change it

    val apps: Var[Seq[MobileApp]] = Var(getAllApps()) //Seq.empty //Var() //TODO how to use Vars??
    val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")

    @dom
    override def element = {

      val tableRows = generateRows.bind //Note!! bind it before passing it to the table

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
    val generateRows = {
      toBindingSeq(apps.value)
        .map(app =>
          <TableRow cells={Seq(app.name, app.company, app.genre, app.price, app.store)}/>)
        .all
        .bind
    }

    def onSmartClose() = navigateTo(history.getLastVisited)

    def connectWith() = apps.value = getAllApps()
  }
}
