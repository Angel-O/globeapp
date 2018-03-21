import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID
import java.time._

package object utils {

  object Bson {
    def parseId(id: String) = {
      Future.fromTry(BSONObjectID.parse(id).map(_.stringify))
    }

    def newId = Some(BSONObjectID.generate.stringify)
  }
  
  object Date {
    def newDate = Some(LocalDate.now())
  }
}