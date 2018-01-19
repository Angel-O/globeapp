package navigation

import URIs._
import router.BrowserHistory

object Navigators {  
  def navigateToForm()(implicit bh: BrowserHistory) = bh.navigateTo(RegisterPageURI)
  def navigateToHome()(implicit bh: BrowserHistory) = bh.navigateTo(HomePageURI)
  def navigateToHello()(implicit bh: BrowserHistory) = bh.navigateTo(HelloPageURI)
}