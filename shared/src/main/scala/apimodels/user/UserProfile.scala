package apimodels.user

import apimodels.common.Entity
import play.api.libs.json.OFormat
import play.api.libs.json.Json
import apimodels.mobile.Genre, Genre._
import apimodels.mobile.GenreFormat

case class UserProfile(
  userId:             String,
  wduhau:             String, // where did you hear about us (collected on sign up)
  additionalInfo:     String,
  subscribed:         Boolean, // TODO move this to user
  favoriteCategories: Seq[Genre],
  favoriteApps:       Seq[String] = Seq.empty,
  _id:                Option[String] = None) extends Entity {
  
  def addFavoriteApp(appId: String) = {
    this copy(favoriteApps = (favoriteApps :+ appId).distinct)
  }
  
  def removeFromFavoriteApps(appId: String) = {
    this copy(favoriteApps = favoriteApps.filterNot(_ == appId))
  }
}

case object UserProfile {
  implicit val genreFormat = GenreFormat
  implicit val reviewFormat: OFormat[UserProfile] = Json.format[UserProfile]

  def apply(
    userId:             String,
    wduhau:             String,
    additionalInfo:     String,
    subscribed:         Boolean,
    favoriteCategories: Seq[Genre],
    favoriteApps:       Seq[String] = Seq.empty,
    _id:                Option[String] = None) =
    new UserProfile(
      userId,
      wduhau,
      additionalInfo,
      subscribed,
      favoriteCategories,
      favoriteApps,
      _id)
}