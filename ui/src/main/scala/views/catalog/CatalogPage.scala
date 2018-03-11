package views.catalog

import components.core.Implicits._
import components.core.Helpers._
import components.Components.Input
import components.Components.Table
import components.Components.Router
import components.Components.Layout
import components.Components.Util
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Login}
import appstate.{MobileAppsSelector, FetchAllMobileApps}
import apimodels.mobileapp.MobileApp
import views.catalog._
import appstate.AppCircuit._
import appstate.MobileAppsSelector._
import org.scalajs.dom.raw.Event

object CatalogPage {

  def view() = new RoutingView() {//{with MobileAppsSelector {

    dispatch(FetchAllMobileApps) // fetching on first load

    lazy val apps: Var[Seq[MobileApp]] = Var(Seq.empty) //TODO how to use Vars??
    val appDialogIsOpen = Var(false)
    val selectedApp = Var[Option[MobileApp]](None)
    //val tableSize = Var(`2/3`) //TODO make it nicer...

    @dom
    override def element = {

      //NOTE 1: calling value rather than bind on apps would cause the
      //fetching apps on first load to fail.
      //Note 2: bind the rows before passing them to the table
      //val tableRows = generateRows(apps.bind).bind
      //Note 3: search box need to come before binding the apps and the
      // rows otherwise a page refresh would be triggered each time
      // the text changes

      //val size = tableSize.bind
      <div>
        <h1>Apps catalog</h1>
        <Box sizes={Seq(`2/3`)} contents={Seq(
          <div>
            <TextInput placeHolder="Search"  
                onChange={handleSearchBoxChange _}/>
          </div>
        )}/>
        { val tableRows = generateRows(apps.bind).bind
          tableRows.isEmpty match {
            case true => 
            <span>No apps to show</span>
            case false => 
            val targetApp = selectedApp.bind
            val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")
            <div>
              <Box sizes={Seq(`2/3`)} contents={Seq(
                <div>
                    <Table isBordered={true} 
                      isStriped={true}
                      isFullWidth={true} 
                      isHoverable={true}
                      header={<TableHeader cells={headers}/>}
                      rows={tableRows}
                      onMouseEnter={handleMouseEnter _}
                      onMouseLeave={handleMouseLeave _}
                      footer={<TableFooter cells={headers}/>}/>
                </div>, 
              { toBindingSeq(targetApp).map( app =>             
                <div style={"position: sticky; top: 0"}> <!-- nice css trick -->
                  <Message header={"App details"} isPrimary={true} 
                    isMedium={true} style={"padding: 1em"} content={ 
                    <div>
                      <h1> Name: { app.name } </h1>
                      <h2> Developed by: { app.company } </h2>
                      <h2> Genre: { app.genre } </h2>
                      <h2> Price: { s"£ ${formatPrice(app.price)}" } </h2>
                      <h2> Store: { app.store } </h2>
                      <h3> Description: Coming soon </h3>
                      <h3> Rating: Coming soon </h3>
                    </div>}/>
                </div> ).all.bind // get the underlying sequence (0 or 1 element)
                .find(_ => true) // calling head would cause an exception (find returns a safe option)
                .getOrElse(<Dummy/>.build.bind) } // return a dummy div if the target app is not selected
              )}/>    
              { //val targetApp = selectedApp.bind
                //TODO display company info instead....
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
      //appDialogIsOpen.value = true
    }

    def handleRowHover(rowIndex: Int) = {
      selectedApp.value = Some(apps.value(rowIndex))
    }

    def handleMouseLeave() = {
      selectedApp.value = None
      //tableSize.value = `3/4`
    }

    def handleMouseEnter() = {
      //tableSize.value = `2/3`
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
           cells={Seq(
             <span style={"text-decoration: underline"}>{ app.name }</span>, 
              app.company, 
              app.genre, 
              formatPrice(app.price), 
              app.store)} 
              onHover={handleRowHover _}
              onClick={_:Int => navigateToMobileAppDetail(app._id)}/>)
        .all
        .bind
      
        // Note: the following won't work becuse the Table expects TableRows not html elements
        // therefore we cannot wrap a row into a div and force the evaluation of the custom
        // component
//       for(app <- toBindingSeq(mobileApps)) yield {
//         println(app.name)
//         <TableRow
//            cells={Seq(app.name, app.company, app.genre, formatPrice(app.price), app.store)}
//            onClick={handleRowClick _}/>
//       }
    }

    //def connectWith() = apps.value = getAllApps()
    connect(apps.value = getAllApps())(mobileAppSelector)
  }
}
