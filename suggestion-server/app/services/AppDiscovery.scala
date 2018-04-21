package services

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import apimodels.mobile.Genre
import apimodels.mobile.MobileApp
import apimodels.poll.Poll

object AppDiscovery {
  
  def findMostDebatedApps(polls: Seq[Poll], apps: Seq[MobileApp], amount: Int)(implicit ec: ExecutionContext) = {
    Future {
      (polls.map(_.mobileAppId) groupBy identity).toSeq
        .sortBy{ case (_, occurrences) => occurrences.size }
        .map{ case (id, _) => id }
        .take(amount)
        .flatMap(id => apps.find(_._id == Some(id)))
    }
  }

  def findRelatedApps(mobileApps: Seq[MobileApp], appId: String)(implicit ec: ExecutionContext) = {
    val keywords = mobileApps
      .find(_._id == Some(appId))
      .map(_.keywords)
      .getOrElse(Seq.empty)
      
    def isRelated(app: MobileApp, appId: String, relatedApps: Seq[MobileApp]) = {
      app.keywords.exists(keywords.contains) && 
      app._id != Some(appId) && 
      !relatedApps.contains(app)
    }

    def findRelatedAppsRecursively(
      mobileApps:  Seq[MobileApp],
      appId:       String,
      keywords:    Seq[String],
      relatedApps: Seq[MobileApp] = Seq.empty): Seq[MobileApp] = {

      mobileApps match {
        case Nil => relatedApps
        case app +: rest =>
          findRelatedAppsRecursively(
            rest,
            appId,
            keywords,
            if (isRelated(app, appId, relatedApps)) relatedApps :+ app
            else relatedApps)
      }
    }

    Future{ findRelatedAppsRecursively(mobileApps, appId, keywords) }
  }
  
  def findAppsByGenres(mobileApps: Seq[MobileApp], genres: Seq[Genre])(implicit ec: ExecutionContext) = {
    Future {
      (for {
        genre <- genres
        apps <- mobileApps.filter(app => app.genre == genre)
      } yield(apps)).distinct
    }
  }
  
  //  private def findRelatedApps2(mobileApps: Seq[MobileApp], appId: String) = {
//    val keywords = mobileApps
//      .find(_._id == Some(appId))
//      .map(_.keywords)
//      .getOrElse(Seq.empty)
//
//    Future {
//      (for {
//        keyword <- keywords
//        relatedApp <- mobileApps.filter(app => app.keywords.contains(keyword) && app._id != Some(appId))
//      } yield (relatedApp)).distinct
//    }
//  }
}