package utils

import org.scalajs.dom.window
import config.{AUTHORIZATION_HEADER_NAME, JWT_ID}
import java.util.Base64.getDecoder
import JsonMiddleware.toJsonValue

object JwtMiddleware {

  def getToken() = window.sessionStorage.getItem(AUTHORIZATION_HEADER_NAME)

  def storeToken(token: String) =
    window.sessionStorage.setItem(AUTHORIZATION_HEADER_NAME, token)

  def removeToken() =
    window.sessionStorage.removeItem(AUTHORIZATION_HEADER_NAME)
   
  def decodeJWT(token: String) = {
    val claimsData = extractClaims(token)
    val json = toJsonValue(decode(claimsData))
    val userJson = (json \ JWT_ID).get //TODO THROW unauthorized error with getOrElse...
    userJson
  }
  private def extractClaims(token: String) = {
    val claims = token.split('.').toSeq match {
      case _ +: payload +: _ => payload //ignoring header and signature
      case _ => "" //TODO provide a sensible fallback value...this will throw an error (or throw unauthorized error)
    }
    claims
  }
  private def decode(str: String): String = new String(getDecoder.decode(str), "UTF-8")
}
