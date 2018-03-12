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
             LocalDate.now),
      Review("1", "user Id2", "mobileAppId2", "Horrible update", LocalDate.now),
      Review("2", "user Id3", "mobileAppId3", "Forget about it", LocalDate.now),
      Review("3", "user Id4", "mobileAppId4", "Cool little app", LocalDate.now),
      Review("4",
             "user Id5",
             "mobileAppId5",
             "Great, keep it up",
             LocalDate.now)
    )
  }
}
