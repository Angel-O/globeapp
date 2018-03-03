package views.catalog

import components.Components.Implicits.{CustomTags2, toHtml, toBindingSeq}
import com.thoughtworks.binding.dom
import hoc.form.LoginForm
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Login}
import org.scalajs.dom.raw.Event

import mock._

//TODO create catalog
object CatalogPage {
  def view() = new RoutingView() with Connect {

    val apps = mock.MobileAppApi.getAll

    val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")

    @dom
    override def element = {
      <div>
			  <h1>Apps catalog</h1>
        <Table 
          header={tableHeader.bind}
          footer={tableFooter.bind}
          isBordered={true}
          isStriped={true}
          isFullWidth={true}
          isHoverable={true}
          rows={rows.bind}/>
      </div>
    }

    @dom
    val tableHeader = {
      <TableHeader cells={headers}/>
    }

    @dom
    val tableFooter = {
      <TableFooter cells={headers}/>
    }

    @dom
    val rows = {
      toBindingSeq(apps)
        .map(app =>
          <TableRow cells={Seq(app.name, app.company, app.genre, app.price, app.store)}/>)
        .all.bind
    }

    def onSmartClose() = navigateTo(history.getLastVisited)
  }
}
