package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.github.dwickern.macros.NameOf.nameOf

import apimodels.review.Review
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.GenericRepository
import repository.SearchCriteria

trait ReviewSearchCriteria extends SearchCriteria {
  def byApp(mobileAppId: String) = obj(nameOf(mobileAppId) -> mobileAppId)
  def byKey(mobileAppId: String, userId: String) = obj(nameOf(mobileAppId) -> mobileAppId, nameOf(userId) -> userId)
}

class ReviewRepository @Inject()(implicit ec: ExecutionContext,
                                    reactiveMongoApi: ReactiveMongoApi)
    extends GenericRepository[Review]("reviews", ec, reactiveMongoApi)
    with ReviewSearchCriteria {

  def getAllByApp(mobileAppId: String): Future[Seq[Review]] = findManyBy(byApp(mobileAppId))
  
  def getByKey(mobileAppId: String, userId: String): Future[Option[Review]] = findOneBy(byKey(mobileAppId, userId))
}
