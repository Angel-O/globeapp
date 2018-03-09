package views.poll

import router.RoutingView
import appstate.PollSelector
import com.thoughtworks.binding.{Binding, dom}, Binding.Var
import components.Components.Implicits.{CustomTags2, toBindingSeq}
import apimodels.poll.Poll
import appstate.FetchPolls
import org.scalajs.dom.raw.Event

object PollsPage {
  def view() = new RoutingView() with PollSelector {

    dispatch(FetchPolls)

    val polls = Var(getPolls())
    val targetPoll = Var[Option[Poll]](None)
    var dialogIsOpen = false

    @dom
    override def element = {
      <div>
      { for(poll <- toBindingSeq(polls.bind)) yield {
        <div onclick={ (_: Event) => openDialog(poll) }>
          	<Card title={ poll.title } subTitle={ poll.createdBy } content={
      		    <div>
          	    { poll.content }
          	  </div>
      		  }/>
        </div> }}
      { val target = targetPoll.bind
        <div>
          <PollDetailDialog 
					dialogIsOpen={ dialogIsOpen } 
					targetPoll={ target } 
					handleClose={ closeDialog _ }/>
				</div>}
      </div>
    }

    def openDialog(poll: Poll) = {
      dialogIsOpen = true
      targetPoll.value = Some(poll)
    }

    def closeDialog() = {
      dialogIsOpen = false
      targetPoll.value = None
    }
    def connectWith() = polls.value = getPolls()
  }
}
