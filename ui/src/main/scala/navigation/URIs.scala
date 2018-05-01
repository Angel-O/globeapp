package navigation

import config.ROOT_PATH

object URIs {
  import router.DynamicRoute._

  val HomePageURI = ROOT_PATH //TODO improve support for slash as baseURL
  val RegisterPageURI = "/register"
  val LoginPageURI = "/login"
  val HelloPageURI = "/hello"
  val UserEditPageURI = "/users"
  val SamplePageURI = "/sample"
  val CatalogPageURI = "/catalog"
  val PollsPageURI = "/polls"
  val FavoriteAppsPageURI = "/favoriteapps"
  val ReviewsPageURI = "/yourreviews"
  val MessagesPageURI = "/yourmessages"
  val UnavailablePageURI = "/unavailable"
  val NotFoundPageURI = "/notfound"
  val UserPostURI = UserEditPageURI.tail / ":username" / "posts" / Int
  val MobileAppDetailPageURI = CatalogPageURI.tail / ":appId"
}
