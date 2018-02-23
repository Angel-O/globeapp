package appstate

import scala.concurrent.ExecutionContext.Implicits.global
import upickle.default._
import apimodels.User
import diode.{Effect, NoAction}
import diode.data.{Ready, Failed, Pot}
import utils.api._, utils.log

//TODO store and pass endpoint root from config
//TODO try and combine multiple effects to use pending state...
//TODO create a EffectWithRedirect that will have a recover checking
// if the response is unauthorized and if so redirect to the login page
// by changing the hash. Use it on those routes that require authentication
object ApiCalls extends Connect {
  
  //TODO move these where the actions are defined!!!

}
//  private def fetchUsers() = Get(url = "http://localhost:9000/api/users")
//  private def createUser(user: User) = Post(url = "http://localhost:9000/api/users", payload = write(user))
//  private def deleteUser(id: String) = Delete(url = "http://localhost:9000/api/users", payload = write(id)) //TODO make it REST
//  private def updateUser(id: String, updated: User) = Put(url = s"http://localhost:9000/api/users/$id", payload = write(updated))

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
//import fr.hmil.roshttp.HttpRequest
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
