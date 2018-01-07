package hoc.form

import scala.util.matching.Regex

object FormValidators {
  
  // allows piping validation rules one after the other, evaluation the
  // next result only if necessary
  implicit class PipeValidation(currentResult: ValidationResult) {
    // using pass by name value for efficiency
    def |>(nextResult: => ValidationResult): ValidationResult = currentResult match {
      case Error(_) | YetToBeValidated => currentResult
      case Success(_) => nextResult
    }
  }
  
  def validateCheckbox(checked: Boolean, customErrorMessage: Option[String] = None) = {
    if(checked) Success("") 
    else customErrorMessage match {
      case Some(msg) => Error(msg)
      case None => Error("Field must be selected")
    }
  }
  
  def validateMatch(thisValue: String, thatValue: String, fieldName: String) = {
    thisValue == thatValue match {
      case true => Success(s"${fieldName}s match")
      case false => Error(s"$fieldName doesn't match")
    }
  }
  
  def emailIsValid(value: String) = EmailValidator.isValid(value) match {
    case true => Success("Valid email")
    case false => Error("Please provide a valid email address")  
  }
  
  def validateAlphaNumericField(value: String, fieldName: String) = {
    def isValid = (ch: Char) => ch.isLetter || ch == ' ' //TODO allow only one space...
    if(!value.forall(isValid)) 
      Error(s"$fieldName can only contain letters")
    else
      Success("Field is valid")
  }
  
  def validateFieldWithRegex(fieldValue: String, fieldName: String, regexPattern: Regex, customErrorMessage: Option[String] = None) = fieldValue match {
    case regexPattern(_) => Success(s"Valid ${fieldName.toLowerCase}")
    case _ => customErrorMessage match {
      case Some(msg) => Error(msg)
      case None => Error(s"Please provide a valid ${fieldName.toLowerCase}") //TODO provide more useful error message
    }
  }
  
  def validateRequiredField(fieldValue: String, fieldName: String = "", customErrorMessage: Option[String] = None) = {
    if(fieldValue.isEmpty) customErrorMessage match {
      case Some(msg) => Error(msg)
      case None => Error(s"$fieldName ${if(fieldName.isEmpty)"F"else"f"}ield is required")
    }
    else Success("")
  }
  
  def validateFieldLength(fieldName: String, fieldValue: String, minLength: Int, maxLength: Int) = {
    val fieldLength = fieldValue.length
    if (fieldLength > maxLength) Error(s"$fieldName field cannot exceed $maxLength characters")
    else if (fieldLength < minLength) Error(s"$fieldName field must be at least $minLength characters long")
    else Success(s"$fieldName field is valid")
  }
}