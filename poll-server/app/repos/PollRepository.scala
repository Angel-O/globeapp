package repos

import scala.concurrent.ExecutionContext

import com.github.dwickern.macros.NameOf._

import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.RepoBase
import apimodels.poll.Poll

class PollRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[Poll]("polls", ec, reactiveMongoApi)