package repos

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import repository.GenericRepository
import apimodels.poll.Poll

class PollRepository @Inject()(implicit ec: ExecutionContext,
                               reactiveMongoApi: ReactiveMongoApi)
    extends GenericRepository[Poll]("polls", ec, reactiveMongoApi)
