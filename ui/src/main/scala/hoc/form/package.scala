package hoc

import com.thoughtworks.binding.dom

package object form {
  import macros.RegisterTag.register

  @register
  val RegistrationForm = "RegistrationForm"

  @register
  val LoginForm = "LoginForm"

  @register
  val CreateReviewForm = "CreateReviewForm"

  @register
  val CreatePollForm = "CreatePollForm"
}
