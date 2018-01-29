package app

import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default._
import apimodels.User
import diode.Effect

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

  def fetchUsersEffect() = Effect(fetchUsers().map(xhr => UsersFetched(read[Seq[User]](xhr.responseText))))
}