package hoc.form

sealed trait ValidationResult{
  
  // allows piping validation results one after the other, evaluating the
  // next result only if necessary. Note: 'this' represents the current result
  // This function is useful to chain validator functions (functions that return
  // a validation result): if the evaluation is expensive e.g. Ajax call to
  // the server you might not want to evaluate the result unless it's stricly necessary
  def |>(nextResult: => ValidationResult): ValidationResult = this match {
      case Error(_) | YetToBeValidated => this
      case Success(_) => nextResult
    }
}
case class Error(message: String) extends ValidationResult
case class Success(message: String) extends ValidationResult
case object YetToBeValidated extends ValidationResult