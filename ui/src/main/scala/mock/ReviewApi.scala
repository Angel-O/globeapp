package mock

import apimodels.review.Review
import java.time._

object ReviewApi {

  def getAll = {
    Seq(
      Review("0",
             "user Id",
             "mobileAppId",
             "Really like this app",
             LocalDate.parse("2007-12-03")),
      Review("1", "user Id2", "mobileAppId2", "Horrible update", LocalDate.parse("2007-12-03")),
      Review("2", "user Id3", "mobileAppId3", "Forget about it", LocalDate.parse("2007-12-03")),
      Review("3", "user Id4", "mobileAppId4", "Cool little app", LocalDate.parse("2007-12-03")),
      Review("4",
             "user Id5",
             "mobileAppId5",
             "Great, keep it up",
             LocalDate.parse("2007-12-03"))
    )
  }
}
