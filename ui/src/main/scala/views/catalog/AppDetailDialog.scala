package views.catalog

import components.core.ComponentBuilder
import components.core.Color
import components.Components.Layout
import components.Components.Modal
import components.core.Implicits._
import components.core.Helpers._
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import apimodels.mobileapp.MobileApp
import utils.nameOf._

case class AppDetailDialogBuilder() extends ComponentBuilder with Color {

  def render = this

  var targetApp: Option[MobileApp] = None
  var dialogIsOpen: Boolean = _
  var handleClose: () => Unit = _
  var priceFormatter: Double => String = _

  @dom
  def build = {

    // turning option into binding seq: if the option is
    // None no element will be mounted into the DOM
    // TODO apply this approach wherever dummy is used
    val dialog = toBindingSeq(targetApp).map(app => {

      val dialogContent =
        <div>
            <Message header={"App details"} isPrimary={true} isMedium={true} style={"padding: 1em"} content={ 
              <div>
                <h1>Name: { app.name } </h1>
                <h2> Developed by: { app.company } </h2>
                <h2> Genre: { app.genre } </h2>
                <h2> Price: { s"£ ${priceFormatter(app.price)}" } </h2>
                <h2> Store: { app.store } </h2>
                <h3> Description: Coming soon </h3>
                <h3> Rating: Coming soon </h3>
              </div>}/>
          </div>

      val modal =
        <div>
            <SimpleModal
                onClose={handleClose} 
                content={dialogContent}
                isOpen={dialogIsOpen}/>
        </div>

      modal
    })

    <div>{ dialog.all.bind }</div>
  }
}
