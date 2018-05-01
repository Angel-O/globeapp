package models.review

import models.common._
import java.time._
import play.api.libs.json.OFormat
import play.api.libs.json.Json
import models.common.Author._
import models.common.Author

case class Review(
  _id:         Option[String],
  author:      Author,
  mobileAppId: String,
  title:       String,
  content:     String,
  rating:      Int,
  dateCreated: Option[LocalDate])
  extends Entity

case object Review {
  implicit val reviewFormat: OFormat[Review] = Json.format[Review]

  def apply(
    _id:         Option[String]    = None,
    author:      Author,
    mobileAppId: String,
    title:       String,
    content:     String,
    rating:      Int,
    dateCreated: Option[LocalDate] = None) =
    new Review(_id, author, mobileAppId, title, content, rating, dateCreated)
}
