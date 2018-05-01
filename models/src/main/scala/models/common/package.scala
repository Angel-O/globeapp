package models

import java.time._

import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

import play.api.libs.json._ // JSON library
//import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

package object common {
  implicit val localDateReads = Reads[LocalDate](js =>
    js.validate[String].map[LocalDate](dtString => LocalDate.parse(dtString)))
  implicit val localDateWrites: Writes[LocalDate] = new Writes[LocalDate] {
    def writes(d: LocalDate): JsValue = JsString(d.toString())
  }
  implicit val localDateFormat: Format[LocalDate] =
    Format(localDateReads, localDateWrites)
    
  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(_.toEpochDay)

  trait Entity {
    val _id: Option[String]
  }
}
