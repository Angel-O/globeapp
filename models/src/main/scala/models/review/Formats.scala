package models.review
import java.time._

import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsString


import play.api.libs.json._ // JSON library
//import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

//import play.api.libs.json.Writes.DefaultLocalDateWrites._
import models.common._
import models.common.Author._

object ReviewFormats {
  
  implicit val reviewReads: Reads[Review] = (
    (__ \ "_id").readNullable[String] and
    (__ \ "author").read[Author] and
    (__ \ "mobileappId").read[String] and
    (__ \ "title").read[String] and
    (__ \ "content").read[String] and
    (__ \ "rating").read[Int] and
    (__ \ "dateCreated").readNullable[String].map(maybeDate => maybeDate.map(LocalDate.parse)))(Review.apply _)

  
  implicit val reviewWrites: Writes[Review] = (
    (__ \ "_id").writeNullable[String] and
    (__ \ "author").write[Author] and
    (__ \ "mobileappId").write[String] and
    (__ \ "title").write[String] and
    (__ \ "content").write[String] and
    (__ \ "rating").write[Int] and
    (__ \ "dateCreated").writeNullable[LocalDate])(unlift(Review.unapply)) 
}