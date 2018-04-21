package controllers

object Config {
  //TO do move this to config file
  // they are all listening to port 9000 because the app runs inside a docker container
  // alongside the other apps: the real ports are exposed to the host, not inside
  // the container
  val APPS_API_ROOT = "http://app:9000/api" // "http://localhost:3001/api"
  val PROFILES_API_ROOT = "http://profile:9000/api" // "http://localhost:3004/api" 
  val POLLS_API_ROOT = "http://poll:9000/api" // "http://localhost:3003/api"
}