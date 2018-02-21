package hoc.form

import components.Components.Implicits.{CustomTags2, ComponentBuilder, _}
import org.scalajs.dom.raw.{
  Event,
  HTMLElement,
  HTMLImageElement,
  HTMLButtonElement
}
import com.thoughtworks.binding.{dom, Binding},
Binding.{Var, Vars, Constants, BindingSeq}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.document
import com.thoughtworks.binding.FutureBinding
import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.js
import upickle.Js
import scala.concurrent.Future
import fr.hmil.roshttp.HttpRequest
import apimodels.User
import appstate.AppCircuit
import diode.Dispatcher
import appstate.FetchUsers
import diode.data.Pot
import diode.data.PotState._
import diode.data.{Ready, Pending}
import appstate.Connect
import appstate.CreateUser

object Styles {
  val labelStyle = "color: white"
}

case class LoginFormBuilder() extends ComponentBuilder {

  def render = this

  var onSubmit: () => Unit = _

  private var username, password = ""
  private val usernameValidation, passwordValidation, loginValidation: Var[ValidationResult] = Var(YetToBeValidated)

  private val handleUsernameChange = (value: String) => {
    username = value.trim()
    validateRequiredField(username, "username", usernameValidation)
    //validateLogin()
  }
  private val handlePasswordChange = (value: String) => {
    password = value //TODO does it need trimming??
    validateRequiredField(password, "password", passwordValidation)
    //validateLogin()
  }
  private def validateRequiredField(value: String, fieldName: String, validation: Var[ValidationResult]) = value.isEmpty match {
    case true => validation.value = Error(s"Please provide a $fieldName")
    case false => validation.value = Success("")
  }
//  private def validateLogin() = (username.toList, password.toList) match {
//    case (Nil, Nil) => loginValidation.value = Error("Please provide username and password")
//    case (Nil, h+:_) => loginValidation.value = Error("Please provide a username")
//    case (h+:_, Nil) => loginValidation.value = Error("Please provide a password")
//    case (u+:_, p+:_) => loginValidation.value = Success("")
//  }
  
  @dom def build = {
    val form =
      <form>
        <div class={ FIELD }>
          <TextInput label={ "Username" } labelStyle={Styles.labelStyle} onChange={ handleUsernameChange }/>
          { renderValidation(usernameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <PasswordInput label={ "Password" } labelStyle={Styles.labelStyle} onChange={ handlePasswordChange } inputValue={ password }/>
          { renderValidation(passwordValidation.bind).bind }
        </div>
        <div>
          { renderSubmitButton(usernameValidation.bind, passwordValidation.bind).bind }
        </div>
        { renderValidation(loginValidation.bind).bind }
      </form>.asInstanceOf[HTMLElement]

    create(form, "login-form")
  }

  @dom def renderSubmitButton(results: ValidationResult*) = {

    val inError = results.exists(_.isInstanceOf[Error])
    val notFullyValidated = results.exists(_ == YetToBeValidated)

    def runValidation() = {
      handleUsernameChange(username)
      handlePasswordChange(password)
    }

    def handleSubmit() = if(notFullyValidated) runValidation() else onSubmit()

    val submitButton = 
      <Button 
				label="Login" 
				isPrimary={true}
    			onClick={handleSubmit _}  
				isDisabled={inError}/>
				
    submitButton
  }

  @dom def renderValidation(result: ValidationResult) = {

    val (errorMsg, successMsg) = result match {
      case Error(msg)       => (msg, "")
      case Success(msg)     => ("", msg)
      case YetToBeValidated => ("", "")
    }

    <FieldValidation errorMessage={errorMsg} successMessage={successMsg}/>
  }
}
