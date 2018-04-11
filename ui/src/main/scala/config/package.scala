package object config {
  val AUTHORIZATION_HEADER_NAME = "Token"
  val AUTH_SERVER_ROOT = "http://localhost:3000"
  val MOBILEAPP_SERVER_ROOT = "http://localhost:3001"
  val REVIEW_SERVER_ROOT = "http://localhost:3002"
  val POLL_SERVER_ROOT = "http://localhost:3003"
  val SUGGESTIONS_SERVER_ROOT = "http://localhost:3004"
  val REQUEST_TIMEOUT = 9000
  val ROOT_PATH = "/globeapp"
  val RESPONSE_TYPE = "text"
  val TEXT_CONTENT_HEADER = ("Content-type" -> "text/plain")
  val JSON_CONTENT_HEADER = ("Content-type" -> "application/json")
}
