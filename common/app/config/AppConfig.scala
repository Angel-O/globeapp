package config

import javax.inject._
import play.api.Configuration
import utils.nameOf._

@Singleton
class AppConfig @Inject() (config: Configuration) {
  
  val underlying = config
  
  object Api{
    
    val TOKEN_HEADER = config.get[String]("play.http.session.jwtName")
    
    val CONTENT = config.get[String]("api.CONTENT")
    
    val REQUEST_TIMEOUT = config.get[Int]("api.REQUEST_TIMEOUT")
    
    val APPS_API_ROOT = config.get[String]("api.APPS_API_ROOT")
    
    val POLLS_API_ROOT = config.get[String]("api.POLLS_API_ROOT")
    
    val PROFILES_API_ROOT = config.get[String]("api.PROFILES_API_ROOT")
  }
}