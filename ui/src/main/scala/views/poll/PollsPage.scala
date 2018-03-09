package views.poll

import router.RoutingView
import appstate.PollSelector
import com.thoughtworks.binding.dom
import components.Components.Implicits.CustomTags2

object PollsPage {
  def view() = new RoutingView() with PollSelector {

    @dom
    override def element = {
      <div>POLLS coming soon...</div>
    }

    def connectWith() = Unit
  }
}
