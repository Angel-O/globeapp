package exceptions

object ServerException{
  case class UnauthorizedException(message: String) extends Exception(message)
  case class ForbiddenException(message: String) extends Exception(message)
  case class NotFoundException(message: String) extends Exception(message)
}