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
}

class UserRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[User]("users", ec, reactiveMongoApi)
    with UserSearchCriteria {

  def getByEmail(email: String): Future[Option[User]] =
    findOneBy(byEmail(email))
}
