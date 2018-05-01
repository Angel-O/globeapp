package models.mobile

import play.api.libs.json.{ OFormat, Json }
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.Format
import play.api.libs.json.Reads
import play.api.libs.json.Writes

import Genre._

object GenreFormat extends Format[Genre] {
  def reads(json: JsValue): JsResult[Genre] = {
    val text = json.as[String].toLowerCase() 
    text match{
    case "education"     => JsSuccess(Education)
    case "entertainment" => JsSuccess(Entertainment)
    case "finance"       => JsSuccess(Finance)
    case "food"          => JsSuccess(Food)
    case "gaming"        => JsSuccess(Gaming)
    case "life-style"    => JsSuccess(LifeStyle)
    case "music"         => JsSuccess(Music)
    case "news"          => JsSuccess(News)
    case "professional"  => JsSuccess(Professional)
    case "social"        => JsSuccess(Social)
    case "weather"       => JsSuccess(Weather)
    case _               => JsError(s"Invalid Genre ($text)")
    }
  } 
  def writes(gender: Genre) = gender match {
    case Education     => JsString("education")
    case Entertainment => JsString("entertainment")
    case Finance       => JsString("finance")
    case Food          => JsString("food")
    case Gaming        => JsString("gaming")
    case LifeStyle     => JsString("life-style")
    case Music         => JsString("music")
    case News          => JsString("news")
    case Professional  => JsString("professional")
    case Social        => JsString("social")
    case Weather       => JsString("weather")
  }
}