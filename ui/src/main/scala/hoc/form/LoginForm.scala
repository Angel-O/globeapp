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
import utils.nameOf._
import appstate.ConnectorBuilder
import FormValidators.validateRequiredField
import hoc.form.common.FormElements._

object Styles {
  val labelStyle = "color: white"
}

case class LoginFormBuilder() extends ConnectorBuilder {

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
  private def validateLogin(codeOption: Option[Int]) = codeOption match {
   case None => loginValidation.value = Success("")
   case Some(code) => { code match{
     case 401 => loginValidation.value = Error("Invalid username and password")
     case _ => loginValidation.value = Error("Something went wrong")
    }
   }
 }
  
  connect()(AppCircuit.authSelector, validateLogin(AppCircuit.authSelector.value.errorCode))

  //TODO use form tag rather than div
  @dom def build = {
    val form =
      <div>
        <div class={ FIELD }>
          <TextInput label={ "Username" } labelStyle={Styles.labelStyle} 
            onChange={ handleUsernameChange } inputValue={username}/>
          { renderValidation(usernameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <PasswordInput label={ "Password" } labelStyle={Styles.labelStyle} 
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
}
