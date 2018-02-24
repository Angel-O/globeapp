package utils

import org.scalajs.dom.window
import config.AUTHORIZATION_HEADER_NAME

object JwtMiddleware {

  def getToken() = window.sessionStorage.getItem(AUTHORIZATION_HEADER_NAME)

  def storeToken(token: String) =
    window.sessionStorage.setItem(AUTHORIZATION_HEADER_NAME, token)

  def removeToken() =
    window.sessionStorage.removeItem(AUTHORIZATION_HEADER_NAME)
}
