package mock

import apimodels.MobileApp

class MobileAppApi {
  def getAll = {
    Seq(MobileApp("1", "Snap", "Snap TM", "Life style", 0.99, "App Store"))
  }
}