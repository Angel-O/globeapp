package hoc.form

import components.Components.Implicits.{CustomTags2, ComponentBuilder}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import appstate.AuthSelector
import common.FormValidators.validateRequiredField
import common._, common.FormElements._
import utils.nameOf._
//import com.thoughtworks.binding.Route


object Styles {
  val labelStyle = "color: white"
}

case class LoginFormBuilder() extends ComponentBuilder with AuthSelector {

  def render = this

  var onSubmit: (String, String) => Unit = _

  private var username, password = ""
  private val usernameValidation, passwordValidation, loginValidation: Var[ValidationResult] = Var(YetToBeValidated)

  private val handleUsernameChange = (value: String) => {
    username = value.trim()
    usernameValidation.value = validateRequiredField(username, nameOf(username), Some(s"Please provide a ${nameOf(username)}"))
    loginValidation.value = YetToBeValidated
  }
  private val handlePasswordChange = (value: String) => {
    password = value
    passwordValidation.value = validateRequiredField(password, nameOf(password), Some(s"Please provide a ${nameOf(password)}"))
    loginValidation.value = YetToBeValidated
  }
  private def setLoginValidation(codeOption: Option[Int]) = codeOption.map { 
      case 401 => loginValidation.value = Error("Invalid username and password")
      case _ => loginValidation.value = Error("Something went wrong")
  }
  
  @dom def build = {
    val form =
      <div>
        <div class={ FIELD }>
          <TextInput label={ "Username" } labelStyle={ Styles.labelStyle } 
            onChange={ handleUsernameChange } inputValue={ username }/>
          { renderValidation(usernameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <PasswordInput label={ "Password" } labelStyle={ Styles.labelStyle } 
            onChange={ handlePasswordChange } inputValue={ password }/>
          { renderValidation(passwordValidation.bind).bind }
        </div>
        <div>
          { renderSubmitButton(label = "Login",
                       isPrimary = true,
                       runValidation = runValidation _,
                       runSubmit = runSubmit _,
                       usernameValidation.bind,
                       passwordValidation.bind).bind }
        </div>
        <br/>
        { renderValidation(loginValidation.bind).bind }  
      </div>

    create(form, "login-form")
  }

  def runValidation() = {
      handleUsernameChange(username)
      handlePasswordChange(password)
  }

  def runSubmit() = onSubmit(username, password)

  def connectWith() = setLoginValidation(getErrorCode())
}
