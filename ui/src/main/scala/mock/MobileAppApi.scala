package mock

import apimodels.mobileapp.MobileApp

object MobileAppApi {
  def getAll = {
    Seq(
      MobileApp("1", "Snap", "Snap TM", "Life style", 0.99, "App Store"),
      MobileApp("2", "Cars 4 you", "Geeks lab", "Gaming", 0.00, "App Store"),
      MobileApp(
        "3",
        "Cars 4 you",
        "Geeks lab",
        "Gaming",
        0.00,
        "Google Store"),
      MobileApp(
        "4",
        "Fiz",
        "Great dreams productions",
        "Finance",
        1.99,
        "App Store"),
      MobileApp(
        "5",
        "Fit-n-ess",
        "R-A apps",
        "Life style",
        5.99,
        "Google Store"))
  }
}