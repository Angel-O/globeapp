package hoc.form

sealed trait ValidationResult
case class Error(message: String) extends ValidationResult
case class Success(message: String) extends ValidationResult
case object YetToBeValidated extends ValidationResult