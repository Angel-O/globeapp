package views.poll

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Color
import components.core.Helpers._
import components.Components.{Layout, Modal, Misc}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import apimodels.poll.Poll
import utils.nameOf._
import java.time.format.DateTimeFormatter
import org.scalajs.dom.raw.Event

case class PollDetailDialogBuilder() extends ComponentBuilder with Color {

  def render = this

  var targetPoll: Option[Poll] = None
  var dialogIsOpen: Boolean = _
  var handleClose: () => Unit = _
  var appName: String = _
  var castVote: (String, Int) => Unit = _
  var canVote: Boolean = _

  private val dateFormatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

  @dom
  def build = {

    val dialog = toBindingSeq(targetPoll).map(poll => {

      val totalVotes = poll.options.map(_.votedBy.size).sum
      def getVotesPercentage(votes: Int) = votes match {
        case 0 => 0.0
        case _ => votes / totalVotes.toDouble * 100
      }
      val results = toBindingSeq(poll.options)
        .map(option => 
          <li>
            <div style={"display: flex; justify-content: space-between"}> {unwrapElement(
              <span style={"cursor: pointer"} onclick={(_: Event) => castVote(poll._id.get, option.id)}>
                <Icon id="thumbs-up"/>
              </span>, canVote).bind}
              <span>{option.content}</span> 
              <span>{s"${getVotesPercentage(option.votedBy.size)} %"}</span> 
            </div>
          </li>)
        .all
        .bind

      val popupHeader = s"Poll: ${poll.title} - (closing date: ${poll.closingDate.format(dateFormatter)})"

      val dialogContent =
        <div>
            <Message header={popupHeader} isPrimary={true} 
            isMedium={true} style={"padding: 1em"} content={ 
              <div>
                <h1> App: { appName } </h1> <br/>
                <h1> Content: { poll.content } </h1> <br/>
                <ul> { results } </ul>
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
