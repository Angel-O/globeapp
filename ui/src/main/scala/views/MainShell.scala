package views

import navigation.URIs._
import components.core.Implicits._
import components.core.BulmaCssClasses
import components.Components.{Navbar, Layout, Misc, Button}
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Constants, BindingSeq}
import navigation.Navigators._
import router.RoutingView
import utils.Push
import config._

import apimodels.message.WsMessage
import apimodels.common.Author
import appstate.AuthSelector._
import java.time._
import utils.api._

import utils.WsMiddleware._

import org.scalajs.dom.raw.WebSocket
import apimodels.message.MessageType._

import appstate.AppCircuit._
import apimodels.message.MessageType

object MainShell extends BulmaCssClasses {
    
  @dom
  def render(content: HTMLElement,
             loggedIn: Boolean,
             username: String,
             notifications: Var[Int],
             socket: WsClient[_, _],
             login: () => Unit,
             logout: () => Unit,
             navigateToCatalog: () => Unit,
             navigateToPolls: () => Unit,
             navigateToFavoriteApps: () => Unit,
             navigateToMessages: () => Unit,
             navigate: () => Unit) = {

    if (loggedIn) {
      def incrementNotifications(msg: MessageType) = {
        println("MESSAGE, RECEIVED...")
        Some(msg) collect { case x: Notification => notifications.value = notifications.value + 1 }
        println(notifications.value)
      }
      socket.handleMessageWith(incrementNotifications)   
    } 
    
    if(socket.isOpen){
      socket.handleCloseWith(() => notifications.value = 0)
    }
    
    val logo =
      <NavbarLogo href={s"#$ROOT_PATH"} image={
        <img src={"https://bulma.io/images/bulma-logo.png" } 
          alt={"Globeapp logo"}
          width={112} height={28}/>}/>
      
    val icon =
      <span class="fa-stack fa-3x" style={"font-size: 0.55em;"}>
        <i class="fa fa-inbox fa-stack-2x"></i> { if(notifications.bind > 0)
        <span class="fa-stack-1x inbox-number">{notifications.bind}</span> else <div></div>}{if(notifications.bind > 0)
        <span class="fa-stack-1x inbox-text">new</span> else <div></div>}
      </span>

    val rightNavbarItems =
      Seq(
        <NavbarItem 
          item={<IconButton isPrimary={true} 
          icon={ icon } 
          label="messages" onClick={() => { notifications.value = 0; navigateToMessages() }}/>}/>,
        <NavbarItem 
          item={<SimpleButton isSuccess={true} 
          icon={ <Icon id="comments"/> } 
          label="polls" onClick={navigateToPolls}/>}/>,
        <NavbarItem 
          item={<SimpleButton isInfo={true} 
          icon={ <Icon id="mobile"/> } 
          label="catalog" onClick={navigateToCatalog}/>}/>,
        <NavbarItem 
          item={<SimpleButton isInfo={true} 
          icon={ <Icon id="user"/> } 
          label="test" onClick={navigate}/>}/>,
        renderAccountMenuItem(loggedIn, username, login, logout).bind
      )

    // TODO consolidate navbarItem and apply same logic for similar situations
    // (note how there is no need to call bind in <NavbarItem item={username}/>)
    val navbar =
      <div>
      <Navbar isFixedTop={false} 
        isTransparent={true} logo={logo}
        rightItems={rightNavbarItems}/></div>

    val banner =
      <Banner content={<h5>Welcome to globeapp</h5>}/>

    socket.send(new WsMessage(
            sender = "5aaee5ff810000a8af9cc6c2", 
            messageType = UserMessage(sender = "5aaee5ff810000a8af9cc6c2", content = "hello mates how y'all doing?", recipient="iii"),
            dateCreated = Some(LocalDate.parse("2007-12-03"))))
    
    
    //The shell
    <div class={getClassName(CONTAINER, FLUID)}>
      {navbar}
      {banner}
      {content}
    </div>
  }

  // Note even if loggedIn is visible within the scope of the renderAccountMenuItem
  // we need to pass it here as the render method needs to be bound to the logged in
  // status Var. (Techincally we don't need to pass it, but it's a good idea to
  // hihglight the dependency of the account menu item on the logged in status, all it's
  // needed is to call .bind on the loggedIn Var BEFORE the renderAccountMenuItem
  // is invoked, then we can just check the value (loggedIn.value))
  @dom def renderAccountMenuItem(loggedIn: Boolean,
                                 username: String,
                                 login: () => Unit,
                                 logout: () => Unit) = {
    if (loggedIn)
      <NavbarItem 
            item={username} 
            isRightDropdown={true} 
            isHoverable={true}
            dropdownItems={Seq(
                <a href={s"#$ROOT_PATH/yourreviews"}>Your reviews</a>, 
                <a href={s"#$ROOT_PATH/favoriteapps"}>Favourite apps</a>, 
                <a href={s"#$ROOT_PATH"}>Contact us</a>, <hr/>, 
                <a href={s"#$ROOT_PATH"}>Manage account</a>,
                <Button label="log out" onClick={logout}/>)} />
    else <NavbarItem item={
          <SimpleButton label="log in" icon={ <Icon id="user"/> } isWarning={true} onClick={login}/>}/>
  }
}
