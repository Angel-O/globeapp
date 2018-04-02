package utils

import config._
import router.HashUpdater
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import diode.NoAction
import diode.Action
import navigation.URIs.{LoginPageURI, UnavailablePageURI}
//import com.github.ghik.silencer.silent;

//TODO rather than hardcoding it take baseUrl from config or routes

// This is a util trait that allows to set the baseUrl once and reuse router
// HashUpdater.push method Note: it is ok to silent the complier because the
// baseUrl is used by a lazily evaluated method
//@silent
trait HashChanger extends HashUpdater { val baseUrl = Some(ROOT_PATH) }

object Redirect {
  implicit class RedirectFuture[B <: Action](x: Future[B]) extends Push {
    import api.getErrorCode

    def redirectOnFailure = {
      x.recoverWith({
        case ex => {
          //TODO create custom error page for different errors
          getErrorCode(ex) match {
            case 401 => push(LoginPageURI)
            case _   => push(UnavailablePageURI)
          }
          Future { NoAction }
        }
      })
    }
  }
}
