package views.home

import navigation.URIs._
import components.core.Implicits._
import components.Components.{Layout, Button, Input, Misc}
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
    lazy val app = Var[Option[MobileApp]](getMobileAppById(appId))

    //TODO use pot data, feetch app by Id and store it in state
    // if fetching fails redirect to 404 or show error msg
    @dom override def element = {

      val appName = app.bind.map(_.name).getOrElse("")
      val appDescription = app.bind.map(_.genre).getOrElse("")
      val reviews = <div>Reviews: { appName }</div>
      val description = <div>Description: { appDescription }</div>
      val createReview =
        <div class={"notification"}> <TextareaInput label={ "Create a review" } /></div>
      val actions =
        <div>
          <SimpleButton icon={<Icon id="star"/>} label={"favorite"}/>
          <SimpleButton icon={<Icon id="clipboard"/>} label={"create poll"}/>
        </div>

      val pageSkeleton =
        <div>
          <Tile isAncestor={true} children={Seq(
            <Tile isVertical={true} children={Seq(
              <Tile isParent={true} children={Seq(
                <Tile isPrimary={true} content={<div>{appName}</div>}/>
              )}/>,
              <Tile children={Seq(
                <Tile width={5} children={Seq(
                  <Tile isParent={true} isVertical={true} children={Seq(
                    <Tile isInfo={true} content={description}/>,
                    <Tile content={actions}/>,
                    <Tile content={createReview}/>
                  )}/>
                )}/>,
                <Tile isParent={ true } children={Seq(
                  <Tile isInfo={true} content={ reviews }/>
                )}/>
              )}/>
            )}/>
          )}/>
        </div>

      pageSkeleton
    }

    connect(app.value = getMobileAppById(appId))(mobileAppSelector)
  }
}
