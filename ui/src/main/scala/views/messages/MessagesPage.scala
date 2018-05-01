package views.messages

import router.RoutingView
import com.thoughtworks.binding.{Binding, dom}, Binding.Var
import components.core.Implicits._
import components.core.Helpers._
import components.Components.Card
import org.scalajs.dom.raw.Event
import appstate.AuthSelector._
import appstate.AppCircuit._
import apimodels.message.MessageType.UserMessage
import apimodels.message.MessageTypeFormat._
import utils.api._, utils.json._
import config._
import scala.concurrent.ExecutionContext.Implicits.global

object MessagesPage {
  def view() = new RoutingView() {

    val messages = Var[Seq[UserMessage]](Seq.empty)
    
    fetchMessages

    @dom
    override def element = {
      <div> { for { message <- toBindingSeq(messages.bind) } yield {
        <div>
          <Card title={ message.content take 10 } subTitle={ "..." } content={
            <div> { message.content } </div>
          }/>
        </div>}}
      </div>
    }
    
    def fetchMessages = {
      Some(getLoggedIn) collect { 
        case true => 
          Get(url = s"$USERMESSAGE_SERVER_ROOT/api/messages") map 
          { data => messages.value = read[Seq[UserMessage]](data.responseText) } 
       }
    }
  }
}
