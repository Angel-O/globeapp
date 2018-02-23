package views

import navigation.URIs._
import components.Components.Implicits._
import org.scalajs.dom.raw.HTMLElement
import com.thoughtworks.binding.dom
import navigation.Navigators._
import router.RoutingView
import appstate.{Connect, Logout}
import utils.Push

object MainShell extends BulmaCssClasses with Connect with Push {

  //TODO extract getClassName into a trait, or even better move it from
  // componentBuilder to bulmaCSSclasses trait
  //TODO create sub-packages for each page
  //TODO clicking on the logo causes an error: investigate

  @dom
  def render(content: HTMLElement) = {
    val logo = <NavbarLogo
                    image={<img 
                            src={"https://bulma.io/images/bulma-logo.png" } 
                            alt={"Globeapp logo"}
                            width={112}
                            height={28}/>}
                    href={"#"}/>

    val logoutButton = <Button label="logout" onClick={doLogout _}/>
    val loginButton = <Button label="login" onClick={navigateToLogin _}/>

    val navbar = <Navbar
                    isFixedTop={false} 
                    isTransparent={true}
                    logo={logo}
                    rightItems={Seq(<NavbarItem item="account" dropdownItems={Seq(logoutButton, loginButton)}/>)}/>

    val shell = <div class="container is-fluid">{navbar}{content}</div>

    shell
  }

  def doLogout() = dispatch(Logout)
  def navigateToLogin() = push("/login")
}
