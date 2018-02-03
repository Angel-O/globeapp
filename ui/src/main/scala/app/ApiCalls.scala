package app

import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default._
import apimodels.User
import diode.Effect
import diode.data.Ready
import org.scalajs.dom.raw.XMLHttpRequest
import diode.data.Pot
import scala.util.Success
import scala.util.Failure
import fr.hmil.roshttp.HttpRequest
import diode.data.Failed

object ApiCalls {
  private def fetchUsers() = {
    val future = Ajax.get(
      url = "http://localhost:9000/api/users", 
      data = null, 
      timeout = 9000, 
      headers = Map.empty, 
      withCredentials = false, 
      responseType = "text")

    future
  }

  
      
    // fut.map(xhr => {
    //   val res = xhr.responseText
    //   val users = read[Seq[User]](res)
    //   users
    // })

    //having this commented out code inside the future call back causes an infinte compilation....
    //val users = readJs[Seq[User]](read(xhr.responseText))
    //users
  
//  import upickle.default._
//    val request = HttpRequest("http://localhost:9000/api/users")
//
//    import monix.execution.Scheduler.Implicits.global
//    import scala.util.{Failure, Success => Ok}
//    //import fr.hmil.roshttp.response.SimpleHttpResponse
//    request.send().map( res => {
//      val users = read[Seq[User]](res.body)
//      log.warn("UOL", users)
//      println("LOU", users)
//      users.foreach(x => log.warn("user:", x.name))
//      users.exists(_.name == userName) match{
//          case true => Error(s"Username $userName already taken")
//          case _ => Success("Valid username, my friend")
//        }
//    })

  def fetchUsersEffect() = {
    Effect(fetchUsers()
        .map(xhr => UsersFetched(Ready(read[Seq[User]](xhr.responseText))))
        .recover({case ex => UsersFetched(Failed(ex))}))
      
  }
}