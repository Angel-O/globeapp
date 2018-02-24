package navigation

import config.ROOT_PATH

object URIs {
  import router.DynamicRoute._

  val HomePageURI = ROOT_PATH //TODO improve support for slash as baseURL
  val RegisterPageURI = "/register"
  val LoginPageURI = "/login" //TODO redirect to login if not authenticated
  val HelloPageURI = "/hello"
  val UserEditPageURI = "/users"
  val SamplePageURI = "/sample"
  val UserPostURI = UserEditPageURI.tail / ":username" / "posts" / Int
}
