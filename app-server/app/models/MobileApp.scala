package models

import apimodels.mobileapp.{MobileApp => ApiMobileApp}
import play.api.libs.json._
import reactivemongo.play.json._

case class MobileApp private (_id: String,
                              name: String,
                              company: String,
                              genre: String,
                              price: Double,
                              store: String,
                              keywords: Seq[String])

case object MobileApp {
  implicit class fromModelToApi(x: MobileApp) {
    def toApi =
      ApiMobileApp(x._id, x.name, x.company, x.genre, x.price, x.store)
  }

  implicit val mobileAppFormat: OFormat[MobileApp] = Json.format[MobileApp]
}

