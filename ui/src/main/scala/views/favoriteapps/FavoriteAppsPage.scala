package views.favoriteapps

import router.RoutingView
import com.thoughtworks.binding.{Binding, dom}, Binding.Var
import components.core.Implicits._
import components.core.Helpers._
import components.Components.Card
import appstate.FetchFavoriteMobileApps
import org.scalajs.dom.raw.Event
import appstate.MobileAppsSelector._
import appstate.AuthSelector._
import appstate.AppCircuit._
import apimodels.mobile.{MobileApp, Genre}

object FavoriteAppsPage {
  def view() = new RoutingView() {

    val apps = Var[Seq[MobileApp]](getMobileApps())

    @dom
    override def element = {
      <div> { for { app <- toBindingSeq(apps.bind) } yield {
        <div>
          <Card title={ app.name } subTitle={ app.company } content={
            <div> { app.store } { app.genre } </div>
          }/>
        </div>}}
      </div>
    }

    connect(apps.value = getMobileApps)(mobileAppSelector)
    // redierct condititon...
    dispatch(FetchFavoriteMobileApps)
  }
}
