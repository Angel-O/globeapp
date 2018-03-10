package views.poll

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Color
import components.core.Helpers._
import components.Components.CustomTags2
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import apimodels.poll.Poll
import utils.nameOf._

case class PollDetailDialogBuilder() extends ComponentBuilder with Color {

  def render = this

  var targetPoll: Option[Poll] = None
  var dialogIsOpen: Boolean = _
  var handleClose: () => Unit = _
  var appName: String = _

  @dom
  def build = {

    val dialog = toBindingSeq(targetPoll).map(poll => {

      val results = toBindingSeq(poll.options)
        .map(option =>
          <div style={"display: flex; justify-content: space-between"}> 
            <span>{option.content}</span> 
            <span>{option.votedBy.size.toString}</span> 
        </div>)
        .all
        .bind

      val dialogContent =
        <div>
            <Message header={"Poll details"} isPrimary={true} 
            isMedium={true} style={"padding: 1em"} content={ 
              <div>
                <h1> Name: { poll.title } </h1>
                <h2> App: { appName } </h2>
                <p> { results } </p>
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
