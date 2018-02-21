
package hoc

package object form {
  import macros.RegisterTag.register
  
  @register
  val RegistrationForm = "RegistrationForm" 
  
  @register
  val LoginForm = "LoginForm"
}