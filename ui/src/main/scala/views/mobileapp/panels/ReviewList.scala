package views.mobileapp.panels

import navigation.URIs._
import components.core.Implicits._
import components.core.Helpers._
import components.Components.{Layout, Button, Input, Misc, Modal}
import router.RoutingView
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import navigation.Navigators._
import appstate.AppCircuit._
import appstate.MobileAppsSelector._
import appstate.ReviewsSelector._
import appstate.AuthSelector._
import appstate.SuggestionsSelector._
import apimodels.mobile.MobileApp
import appstate.{CreateReview, FetchReviews, CreatePoll, UpdateReview, FetchRelatedApps}
import apimodels.review.Review
import appstate.ReviewsFetched
import hoc.form.CreateMessageDialog
import java.time.LocalDate
import utils.api._
import config._
import apimodels.message.MessageType.UserMessage

import utils.WsMiddleware._

import org.scalajs.dom.raw.WebSocket
import apimodels.message.MessageType._

import appstate.AppCircuit._
import apimodels.message.MessageType
import apimodels.message.WsMessage

object ReviewList {
  
  var socket: WsClient[_, _] = new WsClient(
    onConnect = ClientConnected(getUserId),
    onDisconnect = ClientDisconnected(getUserId))
  
  def panel(reviews: Var[Seq[Review]]) = {
    
    socket.open
    
    @dom
    val reviewArea =
      <div>
        Reviews: { toBindingSeq(reviews.bind).map(x =>
        <div>
          <b>{ x.title } - { x.author.name } - { x.dateCreated.map(_.toString).getOrElse("just now") }</b>
          <p> { x.content }</p>{ messageUser(x.author.userId.getOrElse("")).bind }<br/>
        </div>).all.bind }
      </div>
    
    reviewArea
  }
  
  
  // Option[String] gets converted to a String with extra quotes around it !!!! silly!!
  def messageUser(recipient: String) = {

    val popUpIsOpen = Var(false)
    def postMessage(content: String) = {
      if(!socket.isOpen)
      { socket.reconnect }
      
      val message = UserMessage(getUserId, content, recipient)
      Post(url = s"$USERMESSAGE_SERVER_ROOT/api/messages", payload = write(message))
      
      socket.send(WsMessage(getUserId, message, None))
      
      // hack to trigger page reload...TODO fix dialog for good
      popUpIsOpen.value = true
      popUpIsOpen.value = false
    }
    
    @dom
    val element =
    <div style={"display: flex; justify-content: flex-end"}>{val isOpen = popUpIsOpen.bind
      <div>
    		<PageModal 
		  		isDisabled={getUserId == recipient || getUserId.isEmpty || recipient.isEmpty || !getLoggedIn}
		  		icon={<Icon solid={Some(true)} id="envelope"/>}
		  		label={"Send message"} isOpen={isOpen} isPrimary={true} content={
        <div>
        	 <CreateMessageDialog submitLabel={"Send message"} onSubmit={postMessage _} /> 
        </div>
      }/></div>}
    </div>
		  		
		element
  }
}