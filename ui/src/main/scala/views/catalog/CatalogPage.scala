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

  val headers = Seq("Name", "Company", "Genre", "£ Price", "Store")
  
  def view() = new RoutingView() {

    val apps = Var[Seq[MobileApp]](Seq.empty) //TODO how to use Vars??
    val appDialogIsOpen = Var(false)
    val selectedApp = Var[Option[MobileApp]](None)

    @dom
    override def element = {

      <div>
        <h1>Apps catalog</h1>
        <Box sizes={Seq(`2/3`)} contents={Seq(
          <div> <TextInput placeHolder="Search" onChange={handleSearchBoxChange _}/> </div>
        )}/> { val noApps = apps.bind.isEmpty; if(noApps) <span>No apps to show </span> else { val targetApp = selectedApp.bind
        <div>
          <Box sizes={Seq(`2/3`)} contents={Seq(
            <div>
              <Table isBordered={true} isStriped={true} 
                isFullWidth={true} isHoverable={true} 
                onMouseLeave={handleMouseLeave _}
                header={<TableHeader cells={headers}/>}
                rows={toBindingSeq(apps.value).map(app => 
                <TableRow onHover={handleRowHover _} onClick={_:Int => navigateToMobileAppDetail(app._id)} cells={Seq(
                  <span style={"text-decoration: underline"}>{ app.name }</span>, 
                  app.company, 
                  app.genre, 
                  formatPrice(app.price), 
                  app.store) 
                }/>).all.bind} 
                footer={<TableFooter cells={headers}/>
              }/>
            </div>, { toBindingSeq(targetApp).map( app =>  // mapping over the binding option           
            <div style={"position: sticky; top: 0"}> <!-- nice css trick -->
              <Message header={"App details"} isPrimary={true} isMedium={true} style={"padding: 1em"} content={ 
                <div>
                  <h1> Name: { app.name } </h1>
                  <h2> Developed by: { app.company } </h2>
                  <h2> Genre: { app.genre } </h2>
                  <h2> Price: { s"£ ${formatPrice(app.price)}" } </h2>
                  <h2> Store: { app.store } </h2>
                  <h3> Description: Coming soon </h3>
                  <h3> Rating: Coming soon </h3>
                </div>
              }/> 
            </div> ).all.bind.headOption.getOrElse(<Dummy/>.build.bind) } // explained below
          )}/> 
        </div>}} { val dialogIsOpen = appDialogIsOpen.bind //TODO display company info instead....
        <div>
          <AppDetailDialog targetApp={selectedApp.bind} 
            dialogIsOpen={dialogIsOpen}
            handleClose={handleClose _}
            priceFormatter={formatPrice _}/> 
        </div>}     
      </div>
    }

    def handleSearchBoxChange(text: String) = {
      apps.value = getMobileApps() // reset before filtering to avoid filtering over progressively decreasing data
      apps.value = apps.value.filter(app => searchMatchAcrossAllFields(text, app))
    }

    def handleRowClick(rowIndex: Int) = {
      selectedApp.value = Some(apps.value(rowIndex))
    }

    def handleRowHover(rowIndex: Int) = {
      selectedApp.value = Some(apps.value(rowIndex))
    }

    def handleMouseLeave() = {
      selectedApp.value = None
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

    dispatch(FetchAllMobileApps) // fetching on first load
    connect(apps.value = getMobileApps())(mobileAppSelector) //TODO investigate: this is called each time the view is rendered
  }
}

// Explanation: (.all.bind.headOption.getOrElse(<Dummy/>.build.bind))

// all.bind ===> get the underlying sequence (0 or 1 element, since it's an Option), 
// headOption ===> calling head would cause an exception (headOption returns a safe option), 
// getOrElse(<Dummy/>.build.bind) ===> return a dummy div if the target app is not selected