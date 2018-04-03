package views.poll

import router.RoutingView
import appstate.{PollSelector, MobileAppsSelector}
import com.thoughtworks.binding.{Binding, dom}, Binding.Var
import components.core.Implicits._
import components.core.Helpers._
import components.Components.Card
import apimodels.poll.Poll
import appstate.{FetchPolls, FetchMobileApp}
import org.scalajs.dom.raw.Event
import appstate.PollSelector._
import appstate.MobileAppsSelector._
import appstate.AuthSelector._
import appstate.AppCircuit._
import appstate.CastVote

object PollsPage {
  def view() = new RoutingView() {

    val polls = Var[Seq[Poll]](Seq.empty)
    val targetPoll = Var[Option[Poll]](None)
    var dialogIsOpen = false
    var appName = Var("")

    @dom
    override def element = {
      <div> { for (poll <- toBindingSeq(polls.bind)) yield {
        <div onclick={ (_: Event) => openDialog(poll) }>
          <Card title={ poll.title } subTitle={ poll.createdBy.getOrElse("") } content={
            <div> { poll.content } </div>
          }/>
        </div>}} { val target = targetPoll.bind; val app = appName.bind
        <div>
          <PollDetailDialog 
            dialogIsOpen={ dialogIsOpen } 
            targetPoll={ target } 
            appName={ app } 
            handleClose={ closeDialog _ }
            castVote={ castVote _ }
            canVote={ canVote(target) }
          />
        </div>}
      </div>
    }
    
    def canVote(maybePoll: Option[Poll]) = { 
      (for{
        poll <- maybePoll
        pollOption <- poll.options.find(_.votedBy.contains(getUserId)) //TODO fix this...need the userId npt username
      } yield (false)).getOrElse(true)
    }

    def openDialog(poll: Poll) = {
      dialogIsOpen = true
      targetPoll.value = Some(poll)
      dispatch(FetchMobileApp(poll.mobileAppId))
    }

    def closeDialog() = {
      dialogIsOpen = false
      targetPoll.value = None
      appName.value = ""
    }

    def getAppName() = {
      val targetPollAppId = targetPoll.value.map(_.mobileAppId).getOrElse("")
      getMobileAppById(targetPollAppId).map(_.name).getOrElse("")
    }

    def update() = {
      appName.value = getAppName()
      polls.value = getPolls()
      targetPoll.value.map(poll => targetPoll.value = getPollById(poll._id.get))
    }
    
    def castVote(pollId: String, optionId: Int) = {
      dispatch(CastVote(pollId, optionId))
    }

    // multi connect required now since auto unsubscribe has been implemented
    multiConnect(update)(pollSelector, mobileAppSelector)
    dispatch(FetchPolls)
  }
}
