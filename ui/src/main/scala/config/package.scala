package object config {
  
  import Network._
  
  val AUTHORIZATION_HEADER_NAME = "Token"
  val JWT_ID = "user"
  val AUTH_SERVER_ROOT = makeEndpoint(AUTH_SERVER_HOST, AUTH_SERVER_PORT)
  val MOBILEAPP_SERVER_ROOT = makeEndpoint(MOBILEAPP_SERVER_HOST, MOBILEAPP_SERVER_PORT)
  val REVIEW_SERVER_ROOT = makeEndpoint(REVIEW_SERVER_HOST, REVIEW_SERVER_PORT)
  val POLL_SERVER_ROOT = makeEndpoint(POLL_SERVER_HOST, POLL_SERVER_PORT)
  val SUGGESTIONS_SERVER_ROOT = makeEndpoint(SUGGESTIONS_SERVER_HOST, SUGGESTIONS_SERVER_PORT)
  val USERPROFILE_SERVER_ROOT = makeEndpoint(USERPROFILE_SERVER_HOST, USERPROFILE_SERVER_PORT)
  val REQUEST_TIMEOUT = 9000
  val ROOT_PATH = "/globeapp"
  val RESPONSE_TYPE = "text"
  val TEXT_CONTENT_HEADER = ("Content-type" -> "text/plain")
  val JSON_CONTENT_HEADER = ("Content-type" -> "application/json")
}

object Network{
  val AUTH_SERVER_HOST = "auth"
  val AUTH_SERVER_PORT = 3000
  
  val MOBILEAPP_SERVER_HOST = "app"
  val MOBILEAPP_SERVER_PORT = 3001
  
  val REVIEW_SERVER_HOST = "review"
  val REVIEW_SERVER_PORT = 3002
  
  val POLL_SERVER_HOST = "poll"
  val POLL_SERVER_PORT = 3003
  
  val SUGGESTIONS_SERVER_HOST = "suggestion"
  val SUGGESTIONS_SERVER_PORT = 3004
  
  val USERPROFILE_SERVER_HOST = "profile"
  val USERPROFILE_SERVER_PORT = 3005
  
  def makeEndpoint(host: String, port: Int) = s"http://$host:$port"
}
