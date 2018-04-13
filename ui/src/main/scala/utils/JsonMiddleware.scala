package utils

import scala.language.implicitConversions 
import play.api.libs.json.Json._

object JsonMiddleware {
  
  implicit def toJsonValue(responseText: String) = {
    parse(responseText)
  }
}