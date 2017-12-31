package hoc.form

object EmailValidator {
  // match a string with two tokens separated by the @ sign:
  // 1st token: [a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+ (repeat at will)
  // 2nd token: has two sub tokens that will be further checked 
  // 1st sub-token: [a-zA-Z0-9-.]+ (repeat at will)
  // 2nd sub-token: [a-zA-Z0-9]+ (repeat at will, the regex ends with an alphanumeric character)
  private val emailRegex = "(^[a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+[@][a-zA-Z0-9-.]+[a-zA-Z0-9]+)$".r
  def isValid(email: String) = {
    
    def splitAndMatch(email: String) = {
      val splitEmail = email.split('@')
      val domainToken = splitEmail.last
      
      // the domain part of an email can contain multiple chunks separated by a dot
      val chunks = domainToken.split('.') 
      
      // at least a dot needs to be there and each chunk must be non empty, 
      // meaning there aren't two or more consecutive dots
      val isValid = domainToken.exists(_ == '.') && chunks.forall(_.nonEmpty)
      
      isValid
    }
    
    email match {
      case emailRegex(_) => splitAndMatch(email)
      case _ => false
    }
  }
  
}