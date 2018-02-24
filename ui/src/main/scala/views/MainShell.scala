package views

import navigation.URIs._
import components.Components.Implicits._
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.dom
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Logout}
import utils.Push
import config._

object MainShell extends BulmaCssClasses with Connect with Push {

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

    //TODO only show one or the other depending on login status
    val logoutButton = <Button label="logout" onClick={doLogout _}/>
    val loginButton = <Button label="login" onClick={navigateToLogin _}/>
    val navbarItems = Seq(
      <NavbarItem item="account" dropdownItems={Seq(logoutButton, loginButton)}/>)

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
}
