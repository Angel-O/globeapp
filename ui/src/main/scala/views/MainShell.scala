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

object MainShell extends BulmaCssClasses {

  @dom
  def render(content: HTMLElement,
             loggedIn: Boolean,
             username: String,
             login: () => Unit,
             logout: () => Unit,
             navigateToCatalog: () => Unit,
             navigateToPolls: () => Unit,
             navigateToFavoriteApps: () => Unit,
             navigate: () => Unit) = {

    val logo =
      <NavbarLogo href={s"#$ROOT_PATH"} image={
        <img src={"https://bulma.io/images/bulma-logo.png" } 
          alt={"Globeapp logo"}
          width={112} height={28}/>}/>

    val rightNavbarItems =
      Seq(
        <NavbarItem 
          item={<SimpleButton isPrimary={true} 
          icon={ <Icon id="inbox"/> } label="messages"/>}/>,
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

    val banner =
      <Banner content={<h5>Welcome to globeapp</h5>}/>

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
                <a href={s"#$ROOT_PATH"}>Your reviews</a>, 
                <a href={s"#$ROOT_PATH/favoriteapps"}>Favourite apps</a>, 
							  <a href={s"#$ROOT_PATH"}>Stats</a>, 
                <a href={s"#$ROOT_PATH"}>Contact us</a>, <hr/>, 
                <a href={s"#$ROOT_PATH"}>Manage account</a>,
                <Button label="log out" onClick={logout}/>)} />
    else <NavbarItem item={
          <SimpleButton label="log in" icon={ <Icon id="user"/> } isWarning={true} onClick={login}/>}/>
  }
}
