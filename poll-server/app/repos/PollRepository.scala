package repos

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import repository.RepoBase
import apimodels.poll.Poll

class PollRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[Poll]("polls", ec, reactiveMongoApi)
