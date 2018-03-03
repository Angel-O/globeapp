package navigation

import URIs._
import router.BrowserHistory

object Navigators {
  def navigateToLogin()(implicit bh: BrowserHistory) = navigateTo(LoginPageURI)
  def navigateToRegister()(implicit bh: BrowserHistory) =
    navigateTo(RegisterPageURI)
  def navigateToHome()(implicit bh: BrowserHistory) = navigateTo(HomePageURI)
  def navigateToSample()(implicit bh: BrowserHistory) =
    navigateTo(SamplePageURI)
  def navigateToUserEdit()(implicit bh: BrowserHistory) =
    navigateTo(UserEditPageURI)

  def navigateTo(path: String)(implicit bh: BrowserHistory) =
    bh.navigateTo(path)
}
