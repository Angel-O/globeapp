package apimodels.mobile //TODO investigate!!!! calling this package mobileapp makes compilation fail!! crazy

import play.api.libs.json.OFormat
import play.api.libs.json.Json
import apimodels.common.Entity
import play.api.libs.json.JsString

trait Enum[A] {
  trait Value { self: A =>
    _values :+= this
  }
  private var _values = List.empty[A]
  def values = _values
}

sealed trait Genre extends Genre.Value 
case object Genre extends Enum[Genre]{
  import GenreFormat._
  import scala.language.implicitConversions
  implicit def asString(genre: Genre): String = writes(genre).as[String]
  implicit def asGenre(genre: String): Genre = reads(JsString(genre))
    .asOpt.getOrElse(throw new IllegalArgumentException(s"Invalid genre ($genre)"))
    
  case object Education extends Genre; Education
  case object Entertainment extends Genre; Entertainment
  case object Finance extends Genre; Finance
  case object Food extends Genre; Food
  case object Gaming extends Genre; Gaming
  case object LifeStyle extends Genre; LifeStyle
  case object Music extends Genre; Music
  case object News extends Genre; News
  case object Professional extends Genre; Professional
  case object Social extends Genre; Social
  case object Weather extends Genre; Weather
  
  //def values = Seq(Education, Entertainment, Finance, Food, Gaming, LifeStyle, Music, News, Professional, Social, Weather)
}

case object MobileApp {
  implicit val genreFormat = GenreFormat
  implicit val mobileAppFormat: OFormat[MobileApp] = Json.format[MobileApp]

  def apply(_id: Option[String] = None,
            name: String,
            company: String,
            genre: Genre,
            price: Double,
            store: String,
            keywords: Seq[String] = Seq.empty) =
    new MobileApp(_id, name, company, genre, price, store, keywords)
}
case class MobileApp(_id: Option[String],
                     name: String,
                     company: String,
                     genre: Genre,
                     price: Double,
                     store: String,
                     keywords: Seq[String])
    extends Entity