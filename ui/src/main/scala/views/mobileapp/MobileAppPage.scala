package views.home

import navigation.URIs._
import components.core.Implicits._
import components.Components.{Layout, Button}
import router.RoutingView
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import appstate.AppCircuit._
import appstate.MobileAppsSelector._
import apimodels.mobileapp.MobileApp

object MobileAppPage {

  def view() = new RoutingView() {

    lazy val appId = routeParams(0)
    lazy val app = Var[Option[MobileApp]](getAppById(appId))

    //TODO use pot data, feetch app by Id and store it in state
    // if fetching fails redirect to 404 or show error msg
    @dom override def element = {

      val appName = app.bind.map(_.name).getOrElse("")
      val reviews = <div>Reviews: { appName }</div>
      val description = <div>Description</div>
      val actions = <div>Actions</div>;

      <div>
        <Tile isAncestor={true} children={Seq(
          <Tile width={5} children={Seq(
            <Tile isParent={true} isVertical={true} children={Seq(
              <Tile isPrimary={true} content={
                <div> {appName}</div>
              }/>,
              <Tile isInfo={true} content={description}/>,
              <Tile isWarning={true} content={actions}/>
            )}/>
          )}/>,
          <Tile isParent={ true } children={Seq(
            <Tile isDanger={ true } content={ reviews }/>)
          }/>
        )}/>
      </div>
    }

    connect(app.value = getAppById(appId))(mobileAppSelector)
  }
}
