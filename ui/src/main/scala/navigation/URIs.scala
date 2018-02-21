package navigation

object URIs {
  import router.DynamicRoute._
  
  val HomePageURI = "/globeapp" //TODO improve support for slash as baseURL
  val RegisterPageURI = "/register"
  val LoginPageURI = "/login" //TODO redirect to login if not authenticated
  val HelloPageURI = "/hello"
  val UserEditPageURI = "/users"
  val SamplePageURI = "/sample"
  val UserPostURI = UserEditPageURI.tail / ":username" / "posts" / Int
}