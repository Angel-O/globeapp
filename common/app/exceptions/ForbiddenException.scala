package exceptions

object ServerException{
  class ForbiddenException(message: String) extends Exception(message)
}