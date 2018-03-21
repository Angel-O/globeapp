package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.github.dwickern.macros.NameOf.nameOf

import apimodels.review.Review
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi

trait ReviewSearchCriteria extends Criteria {
  def byApp(mobileAppId: String) = obj(nameOf(mobileAppId) -> mobileAppId)
  
  def byKey(mobileAppId: String, `author.userId`: String) =
    obj(nameOf(mobileAppId) -> mobileAppId,
        nameOf(`author.userId`) -> `author.userId`)
        
  def byIdAndUser(_id: String, `author.userId`: String) =
    obj(nameOf(_id) -> _id, nameOf(`author.userId`) -> `author.userId`)
}

class ReviewRepository @Inject()(implicit ec: ExecutionContext,
                                 reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[Review]("reviews", ec, reactiveMongoApi)
    with ReviewSearchCriteria {

  def getAllByApp(mobileAppId: String): Future[Seq[Review]] =
    findManyBy(byApp(mobileAppId))

  def getByKey(mobileAppId: String, userId: String): Future[Option[Review]] =
    findOneBy(byKey(mobileAppId, userId))

  def getByIdAndUser(id: String, userId: String): Future[Option[Review]] =
    findOneBy(byIdAndUser(id, userId))
}
