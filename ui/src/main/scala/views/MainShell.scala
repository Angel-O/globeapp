package views

import navigation.URIs._
import components.Components.Implicits._
import components.NavbarItemBuilder
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Constants, BindingSeq}
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Logout, AuthSelector}
import utils.Push
import config._

object MainShell extends BulmaCssClasses with Push with AuthSelector {

  // State
  val username: Var[String] = Var(getUsername())
  val loggedIn: Var[Boolean] = Var(getLoggedIn())

  // Note even if loggedIn is visible within the scope of the renderAccountMenuItem
  // we need to pass it here as the render method needs to be bound to the logged in
  // status Var. (Techincally we don't need to pass it, but it's a good idea to
  // hihglight the dependency of the account menu item on the logged in status, all it's
  // needed is to call .bind on the loggedIn Var BEFORE the renderAccountMenuItem
  // is invoked, then we can just check the value (loggedIn.value))
  @dom def renderAccountMenuItem(loggedIn: Boolean) = {
    val loginButton =
      <Button label="log in" isPrimary={true} onClick={() => navigateToLogin()}/>
    val logoutButton = <Button label="log out" onClick={doLogout _}/>
    if (loggedIn)
      <NavbarItem 
            item={username} 
            isRightDropdown={true} 
            isHoverable={true}
            dropdownItems={Seq(logoutButton, <hr/>, "Polls", "Favourite apps", "Stats")} />
    else <NavbarItem item={loginButton}/>
  }

  @dom
  def render(content: HTMLElement) = {

    val logo =
      <NavbarLogo href={s"#$ROOT_PATH"} image={
        <img src={"https://bulma.io/images/bulma-logo.png" } 
          alt={"Globeapp logo"}
          width={112} height={28}/>}/>

    val rightNavbarItems =
      Seq(
        <NavbarItem item={<Button label="messages"/>}/>,
        <NavbarItem item={<Button label="catalog"/>}/>,
        renderAccountMenuItem(loggedIn.bind).bind
      )

    val leftNavbarItems =
      Constants("This", "is", "a", "test")
        .map(x => <NavbarItem item={x}/>)
        .all
        .bind

    // TODO consolidate navbarItem and apply same logic for similar situations
    // (note how there is no need to call bind in <NavbarItem item={username}/>)
    val navbar =
      <Navbar isFixedTop={false} 
        isTransparent={true} logo={logo}
        rightItems={rightNavbarItems}
        leftItems={leftNavbarItems}/>

    //The shell
    <div class={getClassName(CONTAINER, FLUID)}>
      {navbar}
      {content}
    </div>
  }

  def doLogout() = dispatch(Logout)
  def navigateToLogin() = push(LoginPageURI)

  // Connect to the auth selector state
  def connectWith() = {
    username.value = getUsername()
    loggedIn.value = getLoggedIn()
  }
}
