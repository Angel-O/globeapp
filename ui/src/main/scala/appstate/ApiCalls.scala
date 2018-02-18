package appstate

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
import diode.NoAction

import components.Components.Implicits.log

object ApiCalls {
  import ApiMiddleware._
  
  //TODO store and pass endpoint root from config
  private def fetchUsers() = Get(url = "http://localhost:9000/api/users")
  private def createUser(user: User) = Post(url = "http://localhost:9000/api/users", payload = write(user))
  private def deleteUser(id: String) = Delete(url = "http://localhost:9000/api/users", payload = write(id)) //TODO make it REST
  private def updateUser(id: String, updated: User) = Put(url = s"http://localhost:9000/api/users/$id", payload = write(updated))

  //TODO try and combine multiple effects to use pending state...
  def fetchUsersEffect() = {
    Effect(fetchUsers()
        .map(xhr => UsersFetched(Ready(read[Seq[User]](xhr.responseText))))
        .recover({case ex => UsersFetched(Failed(ex))}))     
  }
  
  def createUserEffect(user: User) = {
    Effect(createUser(user).map(_ => NoAction)) //could map to a FetchUsers action...
  }
  
  def deleteUserEffect(id: String) = {
    Effect(deleteUser(id).map(_ => NoAction))
  }
  
  def updateUserEffect(id: String, updated: User) = {
    Effect(updateUser(id, updated).map(_ => NoAction))
  }
}

object ApiMiddleware {
  def Post(url: String, payload: Ajax.InputData) = {
    Ajax.post(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }
  
  def Get(url: String) = {
    Ajax.get(
      url = url, 
      data = null, 
      timeout = 9000, 
      headers = Map.empty, 
      withCredentials = false, 
      responseType = "text")
  }
  
  def Delete(url: String, payload: Ajax.InputData) = {  
    Ajax.delete(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }

  def Put(url: String, payload: Ajax.InputData) = {
    Ajax.put(
      url = url, 
      data = payload, 
      timeout = 9000, 
      headers = Map("Content-type" -> "application/json"), 
      withCredentials = false, 
      responseType = "text")
  }
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