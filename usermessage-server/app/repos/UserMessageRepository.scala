package repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.github.dwickern.macros.NameOf.nameOf

// TODO fix mess with message types...
import apimodels.message.MessageType.UserMessage
import apimodels.message.MessageTypeFormat._
import javax.inject.Inject
import play.api.libs.json.Json.obj
import play.modules.reactivemongo.ReactiveMongoApi
import repository.{RepoBase, Criteria}

trait UserMessageSearchCriteria extends Criteria {
  def byRecipient(recipient: String) = obj(nameOf(recipient) -> recipient)
  
  def byRecipientUnread(recipient: String) = {
    val read = false
    obj(nameOf(recipient) -> recipient, nameOf(read) -> read)
  }
}

class UserMessageRepository @Inject()(implicit ec: ExecutionContext,
                                 reactiveMongoApi: ReactiveMongoApi)
    extends RepoBase[UserMessage]("user-messages", ec, reactiveMongoApi)
    with UserMessageSearchCriteria {

  def getAllByRecipient(recipient: String): Future[Seq[UserMessage]] =
    findManyBy(byRecipient(recipient))

  def getAllUnreadByRecipient(recipient: String): Future[Seq[UserMessage]] =
    findManyBy(byRecipientUnread(recipient))
}
