package views.home

import navigation.URIs._
import components.core.Implicits._
import components.Components.{Layout, Button}
import router.RoutingView

import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import appstate.FetchInterestingApps
import appstate.SuggestionsSelector._
import appstate.AppCircuit._
import apimodels.mobile.MobileApp
import hoc.panel.AppsPanel
import appstate.FetchAllMobileApps
import appstate.FetchMostDeabatedApps

object HomePage {

  import navigation.Navigators._
  def view() = new RoutingView() {
    
    val interestingApps: Var[Seq[MobileApp]] = Var(Seq.empty)
    val mostDebatedApps: Var[Seq[MobileApp]] = Var(Seq.empty)
    val maxAmountOfInterestingAppsToShow = 7

    //TODO split into subtiles and create HomePage subpackage
    @dom override def element = {
      <div>
        <Tile isAncestor={ true } children={Seq(
          <Tile isVertical={ true } width={ 8 } children={Seq(
            <Tile children={Seq(
              <Tile isParent={ true } isVertical={ true } children={Seq(
                <Tile isPrimary={ true } content={
                  <div>
                    <p class="title">Vertical...</p>
                    <p class="subtitle">Top tile</p>
                  </div>
                }/>,
                <Tile isWarning={ true } content={
                  <div>
                    <p class="title">...tiles</p>
                    <p class="subtitle">Bottom tile</p>
                  </div>
                }/>)
              }/>,
              <Tile isParent={ true } children={Seq(
                <Tile isInfo={ true } onClick={ navigateToSample _ } content={
                  <div>
                    <p class="title">Middle tile</p>
                    <p class="subtitle">With an image</p>
                    <figure class="image is-4by3">
                      <img src="https://bulma.io/images/placeholders/640x480.png"/>
                    </figure>
                  </div>
                }/>)
              }/>)
            }/>,
            <Tile isParent={ true } children={Seq(
              <Tile isDanger={ true } content={
                <div>
                  <p class="subtitle">Hot area</p>
                  <div class="content">
                    { mostDebatedAppsPanel.bind }
                  </div>
                </div>
              }/>)
            }/>)
          }/>,
          <Tile isParent={ true } children={Seq(
            <Tile isSuccess={ true } onClick={ navigateToRegister _ } content={
              <div class="content">
                <p class="title">Check these out!</p>
                <div class="content">
                  { interestingAppsPanel.bind }
                </div>
              </div>
            }/>)
          }/>)
        }/>
      </div>
    }
    
    @dom val interestingAppsPanel = {
      val apps = interestingApps.bind
      <div><AppsPanel header="Recommended for you" apps={apps} isWarning={true}/></div>
    }
    
    @dom val mostDebatedAppsPanel = {
      val apps = mostDebatedApps.bind
      <div><AppsPanel header="People are talking about these" apps={apps} isInfo={true}/></div>
    }
    
    def update() = {
      mostDebatedApps.value = getMostDebatedMobileApps()
      interestingApps.value = getInterestingMobileApps(maxAmountOfInterestingAppsToShow)
    }
    
    dispatch(FetchAllMobileApps)
    dispatch(FetchInterestingApps)
    dispatch(FetchMostDeabatedApps(10))
    connect(update())(suggestionSelector)
  }
}
