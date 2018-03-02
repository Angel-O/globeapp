package models
import reactivemongo.bson.BSONObjectID
import scala.language.postfixOps
import apimodels.{MobileApp => ApiApp}

case class MobileApp private (_id: BSONObjectID,
                              name: String,
                              company: String,
                              genre: String,
                              price: Double,
                              store: String)

case object MobileApp {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import reactivemongo.play.json._

  def apply(name: String,
            company: String,
            genre: String,
            price: Double,
            store: String) = {

    val _id: BSONObjectID = BSONObjectID.generate
    new MobileApp(_id, name, company, genre, price, store)
  }

  def apply(apiApp: ApiApp) = {
    new MobileApp(BSONObjectID.parse(apiApp.id).get,
                  apiApp.name,
                  apiApp.company,
                  apiApp.genre,
                  apiApp.price,
                  apiApp.store)
  }

  implicit val userFormat: OFormat[MobileApp] = Json.format[MobileApp]
}

object Helpers {
  implicit class fromModelToApi(x: MobileApp) {
    def toApi =
      ApiApp(x._id.stringify, x.name, x.company, x.genre, x.price, x.store)
  }
  implicit class fromApiToModel(x: ApiApp) {
    def toModel =
      MobileApp(BSONObjectID.parse(x.id).get,
                x.name,
                x.company,
                x.genre,
                x.price,
                x.store)
  }
}
