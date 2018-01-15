package navigation

import URIs._
import router.BrowserHistory

object Navigators {  
  def navigateToForm(bh: BrowserHistory) = bh.navigateTo(RegisterPageURI)
  def navigateToHome(bh: BrowserHistory) = bh.navigateTo(HomePageURI) 
  def navigateToHello(bh: BrowserHistory) = bh.navigateTo(HelloPageURI)
}