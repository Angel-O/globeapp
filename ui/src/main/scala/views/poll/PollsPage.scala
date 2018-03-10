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
import appstate.AppCircuit._

object PollsPage {
  def view() = new RoutingView() {//with PollSelector with MobileAppsSelector{
    
    val polls = Var[Seq[Poll]](Seq.empty)
    val targetPoll = Var[Option[Poll]](None)
    var dialogIsOpen = false
    var appName = Var("")

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
        val app = appName.bind
        <div>
          <PollDetailDialog 
					dialogIsOpen={ dialogIsOpen } 
					targetPoll={ target } 
					appName={app}
					handleClose={ closeDialog _ }/>
				</div>}
      </div>
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
      getAppById(targetPollAppId).map(_.name).getOrElse("")
    }
    
    def update() = {
      appName.value = getAppName()
      polls.value = getPolls()
    }
    
    connect(polls.value = getPolls())//(pollSelector)
    connect(appName.value = getAppName())//(mobileAppSelector)
    //multiConnect(update)(pollSelector, mobileAppSelector)
    dispatch(FetchPolls)
  }
}
