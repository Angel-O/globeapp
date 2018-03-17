package mock

import apimodels.review.Review
import java.time._

object ReviewApi {

  def getAll = {
    Seq(
      Review(
        Some("0"),
        "user Id",
        "mobileAppId",
        "Really like this app",
        "Yeah for sure",
        5,
        LocalDate.parse("2007-12-03")),
      Review(Some("1"), "user Id2", "mobileAppId2", "Horrible update", "bye", 2, LocalDate.parse("2007-12-03")),
      Review(Some("2"), "user Id3", "mobileAppId3", "Forget about it", "bye", 2, LocalDate.parse("2007-12-03")),
      Review(Some("3"), "user Id4", "mobileAppId4", "Cool little app", "bye", 2, LocalDate.parse("2007-12-03")),
      Review(
        Some("4"),
        "user Id5",
        "mobileAppId5",
        "Great, keep it up",
        "bye", 2,
        LocalDate.parse("2007-12-03")))
  }
}
