import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID

package object utils {

  object Bson {
    def parseId(id: String) = {
      Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
    }

    def newId = Some(BSONObjectID.generate.stringify)
  }
}