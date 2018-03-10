package views.catalog

import components.core.Implicits._
import components.core.Helpers._
import components.Components.Input
import components.Components.Table
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Login}
import appstate.{MobileAppsSelector, FetchAllMobileApps}
import apimodels.mobileapp.MobileApp
import views.catalog._

object CatalogPage {

  def view() = new RoutingView() with MobileAppsSelector {

    dispatch(FetchAllMobileApps) // fetching on first load

    val apps: Var[Seq[MobileApp]] = Var(Seq.empty) //TODO how to use Vars??
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

      <div>
			  <h1>Apps catalog</h1>
          <TextInput placeHolder="Search"  
            onChange={handleSearchBoxChange _}/>
          { val tableRows = generateRows(apps.bind).bind
            tableRows.isEmpty match {
              case true => 
              <span>No apps to show</span>
              case false => 
              val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")
              <div>
                <Table isBordered={true} 
                  isStriped={true}
                  isFullWidth={true} 
                  isHoverable={true}
                  header={<TableHeader cells={headers}/>}
                  rows={tableRows}
                  footer={<TableFooter cells={headers}/>}/>
                { //val targetApp = selectedApp.bind
                  val dialogIsOpen = appDialogIsOpen.bind
                  <div>
                    <AppDetailDialog targetApp={selectedApp.bind} 
                      dialogIsOpen={dialogIsOpen}
                      handleClose={handleClose _}
                      priceFormatter={formatPrice _}/> 
                  </div>}     
              </div> } 
          }
      </div>
    }

    def handleSearchBoxChange(text: String) = {
      apps.value = getAllApps() // reset before filtering to avoid filtering over progressively decreasing data
      apps.value =
        apps.value.filter(app => searchMatchAcrossAllFields(text, app))
    }

    def handleRowClick(rowIndex: Int) = {
      //TODO probably this needs Vars...they go hand in hand:
      //there is no need for intermediated updates...same below...
      selectedApp.value = Some(apps.value(rowIndex))
      appDialogIsOpen.value = true
    }

    def handleClose() = {
      appDialogIsOpen.value = false
      selectedApp.value = None
    }

    def searchMatchAcrossAllFields(text: String, app: MobileApp) = {
      val appToStringLiteral =
        s"${app.name}${app.company}${app.genre}${formatPrice(app.price)}${app.store}"

      appToStringLiteral.toLowerCase.contains(text.toLowerCase)
    }

    def formatPrice(price: Double) = if (price > 0) price.toString else "FREE"

    @dom
    def generateRows(mobileApps: Seq[MobileApp]) = {
      toBindingSeq(mobileApps)
        .map(app => <TableRow 
            cells={Seq(app.name, app.company, app.genre, formatPrice(app.price), app.store)} 
            onClick={handleRowClick _}/>)
        .all
        .bind
    }

    def connectWith() = apps.value = getAllApps()
  }
}
