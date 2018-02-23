package navigation

import URIs._
import router.BrowserHistory

object Navigators {
  def navigateToLogin()(implicit bh: BrowserHistory) =
    bh.navigateTo(LoginPageURI)
  def navigateToRegister()(implicit bh: BrowserHistory) =
    bh.navigateTo(RegisterPageURI)
  def navigateToHome()(implicit bh: BrowserHistory) = bh.navigateTo(HomePageURI)
  def navigateToSample()(implicit bh: BrowserHistory) =
    bh.navigateTo(SamplePageURI)
  def navigateToUserEdit()(implicit bh: BrowserHistory) =
    bh.navigateTo(UserEditPageURI)
}
