package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.github.dwickern.macros.NameOf.nameOf

import apimodels.mobile.MobileApp
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.GenericRepository
import repository.SearchCriteria

trait MobileAppSearchCriteria extends SearchCriteria {
  def byKey(name: String, company: String, store: String) =
    obj(nameOf(name) -> name,
        nameOf(company) -> company,
        nameOf(store) -> store)
}

class MobileAppRepository @Inject()(implicit ec: ExecutionContext,
                                    reactiveMongoApi: ReactiveMongoApi)
    extends GenericRepository[MobileApp]("mobile-apps", ec, reactiveMongoApi)
    with MobileAppSearchCriteria {

  def getByKey(name: String,
               company: String,
               store: String): Future[Option[MobileApp]] =
    findOneBy(byKey(name, company, store))
}
