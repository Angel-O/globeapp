package repos

import scala.concurrent.ExecutionContext

import apimodels.user.User
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.GenericRepository
import repository.SearchCriteria
import scala.concurrent.Future

trait UserSearchCriteria {
  def byEmail(email: String) = obj("email" -> email)
}

class UserRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends GenericRepository[User]("users") with UserSearchCriteria {
  
  def getByEmail(email: String):Future[Option[User]] = findOneBy(byEmail(email))
}
