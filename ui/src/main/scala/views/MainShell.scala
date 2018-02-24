package views

import navigation.URIs._
import components.Components.Implicits._
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.{dom, Binding}, Binding.{Var}
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Logout, AuthSelector}
import utils.Push
import config._

object MainShell extends BulmaCssClasses with Push with AuthSelector {

  // State
  val username: Var[String] = Var(getUsername())
  val loggedIn: Var[Boolean] = Var(getLoggedIn())
  //TODO create sub-packages for each page
  @dom
  def render(content: HTMLElement) = {

    val logo = <NavbarLogo
                    image={<img 
                            src={"https://bulma.io/images/bulma-logo.png" } 
                            alt={"Globeapp logo"}
                            width={112}
                            height={28}/>}
                    href={s"#$ROOT_PATH"}/>

    val (button, displayText) = loggedIn.bind match {
      case true =>
        (<Button label="logout" onClick={doLogout _}/>, username.value)
      case false =>
        (<Button label="login" onClick={() => navigateToLogin()}/>, "Account")
    }

    val navbarItems = Seq(
      <NavbarItem item={displayText} dropdownItems={Seq(button)} isRightDropdown={true} isHoverable={true}/>)
    //note how there is no need to call bind TODO consolidate navbarItem and apply same logic for similar situations
    //<NavbarItem item={username}/>)

    val navbar = <Navbar
                    isFixedTop={false} 
                    isTransparent={true}
                    logo={logo}
                    rightItems={navbarItems}/>

    val shell = <div class={getClassName(CONTAINER, FLUID)}>
                  {navbar}
                  {content}
                </div>

    shell
  }

  def doLogout() = dispatch(Logout)
  def navigateToLogin() = push(LoginPageURI)

  // Connect to the auth selector state
  def connectWith() = {
    username.value = getUsername()
    loggedIn.value = getLoggedIn()
  }
}
