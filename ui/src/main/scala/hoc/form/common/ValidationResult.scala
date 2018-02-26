package hoc.form.common

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

case object ValidationResult {
  implicit def validationResultAsFuture(result: => ValidationResult) = Future {
    result
  }
  implicit def validationResultAsBoolean(result: => ValidationResult) =
    result match {
      case _ @Success(_) => true
      case _             => false
    }
}
sealed trait ValidationResult {

  // allows piping validation results one after the other, evaluating the
  // next result only if necessary. Note: 'this' represents the current result
  // This function is useful to chain validator functions (functions that return
  // a validation result): if the evaluation is expensive e.g. Ajax call to
  // the server you might not want to evaluate the result unless it's stricly necessary
  def |>(nextResult: => ValidationResult): ValidationResult = this match {
    case Error(_) | YetToBeValidated => this
    case Success(_)                  => nextResult
  }

  def isError = this.isInstanceOf[Error]
  def isSuccess = this.isInstanceOf[Success]
  def isNotValidated = this == YetToBeValidated
}
case class Error(message: String) extends ValidationResult
case class Success(message: String) extends ValidationResult
case object YetToBeValidated extends ValidationResult
