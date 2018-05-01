package models.poll

import java.time._
import PollFormats._
import play.api.libs.json.OFormat
import play.api.libs.json.Json
import models.common._

sealed trait PollStatus
case object Open extends PollStatus
case object Closed extends PollStatus

case class Poll(
  title:       String,
  content:     String,
  mobileAppId: String,
  createdBy:   String,
  closingDate: LocalDate,
  status:      PollStatus,
  options:     Seq[PollOption],
  _id:         Option[String] = None) extends Entity

case object Poll {
  implicit val format: OFormat[Poll] = Json.format[Poll]
  def apply(
    title:       String,
    content:     String,
    mobileAppId: String,
    createdBy:   String,
    closingDate: LocalDate,
    status:      PollStatus,
    options:     Seq[PollOption],
    _id:         Option[String]  = None) =
    new Poll(
      title,
      content,
      mobileAppId,
      createdBy,
      closingDate,
      status,
      // the id will be unique within the scope of a poll
      options.zipWithIndex.map({ case (po, i) => po.copy(id = i) }),
      _id)
  
  def addVote(userId: String, optionId: Int, poll: Poll) = {
    poll.options
    .find(_.id == optionId)
    .map(po => po.copy(votedBy = po.votedBy :+ userId))
    .map(updatedOption => poll.options.map(po => if (po.id == optionId) updatedOption else po))
    .map(updatedOptions => poll.copy(options = updatedOptions))
  }
}

case class PollOption private(content: String, id: Int = 0, votedBy: Seq[String] = Seq.empty)
case object PollOption {
  def apply(content: String) = new PollOption(content)
}

