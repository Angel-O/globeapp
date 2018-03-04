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
    var filterText = Var("")
    val appDialogIsOpen = Var(false)
    val selectedApp = Var[Option[MobileApp]](None)

    @dom
    override def element = {

      //NOTE 1: calling value rather than bind on apps would cause the
      //fetching apps on first load to fail.
      //Note 2: bind the rows before passing them to the table
      //val tableRows = generateRows(apps.bind).bind
      //Note 3: search box need to come before binding the apps and the
      // rows otherwise a page refresh would be triggered each time
      // the text changes

      val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")

      <div>
			  <h1>Apps catalog</h1>
          <TextInput placeHolder="Search" 
            inputValue={filterText.bind} 
            onChange={handleSearchBoxChange _}/>
          { val tableRows = generateRows(apps.bind).bind
            tableRows.isEmpty match {
              case true => 
              <span>No apps to show</span>
              case false => 
              <div>
                <Table isBordered={true} 
                  isStriped={true}
                  isFullWidth={true} 
                  isHoverable={true}
                  header={<TableHeader cells={headers}/>}
                  rows={tableRows}
                  footer={<TableFooter cells={headers}/>}/>
                { renderDialog(selectedApp.bind, appDialogIsOpen.bind).bind }
              </div> } 
          }
      </div>
    }

    def handleSearchBoxChange(text: String) = {
      filterText.value = text
      apps.value = getAllApps() // reset before filtering to avoid filtering over progressively decreasing data
      apps.value = apps.value.filter(app => filterAcrossAllFields(text, app))
    }

    def handleRowClick(rowIndex: Int) = {
      selectedApp.value = Some(apps.value(rowIndex))
      println(selectedApp.value.map(_.name).get)
      appDialogIsOpen.value = true
    }

    @dom
    def renderDialog(targetApp: Option[MobileApp], dialogIsOpen: Boolean) = {
 
      // turning option into binding seq: if the option is 
      // None no element will be mounted into the DOM
      // TODO apply this approach wherever dummy is used
      val dialog = toBindingSeq(targetApp).map(app => {
        
        val dialogContent =
          <div style={"background: white; padding: 1em"}>
            <h1> Name: { app.name } </h1>
            <h2> Developed by: { app.company } </h2>
            <h2> Genre: { app.genre } </h2>
            <h2> Price: { s"£ ${formatPrice(app.price)}" } </h2>
            <h2> Store: { app.store } </h2>
            <h3> Description: Coming soon </h3>
            <h3> Rating: Coming soon </h3>
          </div>

        val modal = 
          <div>
            <SimpleModal
              onClose={handleClose _} 
              content={dialogContent}
              isOpen={dialogIsOpen}/>
          </div>

        modal
      })

      dialog.all.bind
    }

    def filterAcrossAllFields(text: String, app: MobileApp) = {
      val appToStringLiteral =
        s"${app.name}${app.company}${app.genre}${formatPrice(app.price)}${app.store}"
      //val appToStringLiteral = app.toString.toLowerCase ... ==> not good enough
      //val appToStringLiteral = ccToMap(app).map(_._2).foldLeft("")(_ + _).toLowerCase ==> not working on scalaJS
      appToStringLiteral.toLowerCase.contains(text.toLowerCase)
    }

    def formatPrice(price: Double) = if (price > 0) price.toString else "FREE"

    @dom
    def generateRows(mobileApps: Seq[MobileApp]) = {
      toBindingSeq(mobileApps)
        .map(app =>
          <TableRow cells={Seq(app.name, app.company, app.genre, formatPrice(app.price), app.store)} onClick={handleRowClick _}/>)
        .all
        .bind
    }

    //def onSmartClose() = navigateTo(history.getLastVisited)

    def handleClose() = {
      appDialogIsOpen.value = false
      selectedApp.value = None
    }

    def connectWith() = apps.value = getAllApps()

    // uses reflection...cool, but not suitable for scalaJS
    // def ccToMap(cc: AnyRef) =
    //   (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
    //     (a, f) =>
    //       f.setAccessible(true)
    //       a + (f.getName -> f.get(cc))
    //   }
  }
}
