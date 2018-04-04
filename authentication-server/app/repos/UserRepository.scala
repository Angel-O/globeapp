package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.github.dwickern.macros.NameOf.nameOf

import apimodels.user.User
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.{RepoBase, Criteria}

trait UserSearchCriteria extends Criteria {
  def byEmail(email: String) = obj(nameOf(email) -> email)
  def byCredentials(username: String, password: String) = obj(nameOf(username) -> username, nameOf(password) -> password)
}

class UserRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[User]("users", ec, reactiveMongoApi)
    with UserSearchCriteria {

  def getByEmail(email: String): Future[Option[User]] =
    findOneBy(byEmail(email))
    
  def getApiUserByCredentials(username: String, password: String) = {
    for{
      maybeUser <- findOneBy(byCredentials(username, password))
      apiUser <- Future{ maybeUser.map(user => User(_id = user._id, username = user.username)) }
    } yield(apiUser)
  }
}
