package repos

import scala.concurrent.ExecutionContext

import com.github.dwickern.macros.NameOf._

import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.{RepoBase, Criteria}
import apimodels.user.UserProfile

trait UserProfileSearchCriteria extends Criteria {
  def byUser(userId: String) = obj(nameOf(userId) -> userId)
}

class UserProfileRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[UserProfile]("user-profile", ec, reactiveMongoApi) with UserProfileSearchCriteria {
  
  def getByUser(userId: String) = findOneBy(byUser(userId))
}