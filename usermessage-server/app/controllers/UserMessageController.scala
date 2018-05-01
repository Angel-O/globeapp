package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import apimodels.message.MessageTypeFormat._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Json._
import repos.UserMessageRepository
import utils.Bson._
import utils.Date._
import utils.FutureImplicits._
import utils.Json._
import exceptions.ServerException._
import apimodels.common.Entity

class ReviewController @Inject() (
  scc:        SecuredControllerComponents,
  repository: UserMessageRepository)
  extends SecuredController(scc) {

  def getAllByUser = AuthenticatedAction.async { implicit req =>
    val userId = req.user._id.get
    Logger.info("Fetching all messages")
    repository.getAllByRecipient(userId).map(messages => Ok(toJson(messages)))
  }

  def getAllUnreadByuser = AuthenticatedAction.async { implicit req => 
    val userId = req.user._id.get
    Logger.info(s"Fetching all unread messages (userId = $userId)")
    repository.getAllUnreadByRecipient(userId).map(messages => Ok(toJson(messages)))
  }
  
  def saveMessage = AuthenticatedAction.async(parse.json) { implicit req =>
    Logger.info(s"Saving message)")
    (for{
      payload <- parsePayload(req)
      id <- repository.addOne(payload)
    } yield (Ok(id))).logFailure.handleRecover 
  }
}
