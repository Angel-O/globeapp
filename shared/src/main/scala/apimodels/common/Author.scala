package apimodels.common

import play.api.libs.json.OFormat
import play.api.libs.json.Json

case class Author(userId: Option[String],
                  name: String)
                  
case object Author{
  implicit val authorFormat: OFormat[Author] = Json.format[Author]
  def apply(userId: Option[String] = None, name: String) =  new Author(userId, name)
}