package mock

import apimodels.mobile.MobileApp
import scala.concurrent.Future
import scala.concurrent.Promise
import scalajs.js

object MobileAppApi {
  def getAll = {
    Seq(
      MobileApp(Some("1"), "Snap", "Snap TM", "Life style", 0.99, "App Store"),
      MobileApp(Some("2"), "Cars 4 you", "Geeks lab", "Gaming", 0.00, "App Store"),
      MobileApp(
        Some("3"),
        "Cars 4 you",
        "Geeks lab",
        "Gaming",
        0.00,
        "Google Store"),
      MobileApp(
        Some("4"),
        "Fiz",
        "Great dreams productions",
        "Finance",
        1.99,
        "App Store"),
      MobileApp(
        Some("5"),
        "Fit-n-ess",
        "R-A apps",
        "Life style",
        5.99,
        "Google Store"))
  }
  def getById = {
    MobileApp(Some("1"), "Snap", "Snap TM", "Life style", 0.99, "App Store")
  }
}