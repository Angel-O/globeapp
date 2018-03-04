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
    var filterText = Var("")

    @dom
    override def element = {

      //val allApps = Var(apps.bind)
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
            inputValue={filterText.bind} 
            onChange={handleSearchBoxChange _}/>
          { val tableRows = generateRows(apps.bind).bind
          <div>
            <Table isBordered={true} 
              isStriped={true}
              isFullWidth={true} 
              isHoverable={true}
              header={<TableHeader cells={headers}/>}
              rows={tableRows}
              footer={<TableFooter cells={headers}/>}/>
          </div> }
      </div>
    }

    def handleSearchBoxChange(text: String) = {
      filterText.value = text
      apps.value = getAllApps() // reset before filtering to avoid filtering over progressively decreasing data
      apps.value = apps.value.filter(app => filterAcrossAllFields(text, app))
    }

    def filterAcrossAllFields(text: String, app: MobileApp) = {
      val appToStringLiteral = s"${app.name}${app.company}${app.genre}${app.price}${app.store}"
      //val appToStringLiteral = app.toString.toLowerCase ... ==> not good enough
      //val appToStringLiteral = ccToMap(app).map(_._2).foldLeft("")(_ + _).toLowerCase ==> not working on scalaJS
      appToStringLiteral.toLowerCase.contains(text.toLowerCase)
    }

    @dom
    def generateRows(mobileApps: Seq[MobileApp]) = {
      @dom def formatPrice(price: Double) = if (price > 0) price else "FREE"
      toBindingSeq(mobileApps)
        .map(app =>
          <TableRow cells={Seq(app.name, app.company, app.genre, formatPrice(app.price).bind, app.store)}/>)
        .all
        .bind
    }

    def onSmartClose() = navigateTo(history.getLastVisited)

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
