package hoc.form

import components.Components.Implicits.{ CustomTags2, ComponentBuilder, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{ dom, Binding }, Binding.{ Var, Vars, Constants, BindingSeq }
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
import appstate.Connect
import appstate.AppModel
import appstate.ConnectorBuilder
import hoc.form.common.FormElements._
import hoc.form.common._
import common.FormValidators._
import common._, common.Styles
import appstate.AuthSelector


case class RegistrationFormBuilder() extends ComponentBuilder with AuthSelector {
   
  def render = this 
  
  var onSubmit: (String, String, String, String, String) => Unit = _
  var verifyUsernameAlreadyTaken: String => Unit = _
  
  private var subscribeMe: Boolean = false // no need to use Var as there is no need to reload (no validation happening)
  private var termsAccepted: Var[Boolean] = Var(false)
  private val name, username, email, message, whereDidYouHearAboutUs, gender, 
  subscriptionType, password, confirmPassword: Var[String] = Var("")
  private val nameValidation, emailValidation, messageValidation, whereDidYouHearAboutUsValidation, 
  genderValidation, usernameValidation, subscriptionTypeValidation, passwordValidation,
  acceptTermsValidation, confirmPasswordValidation: Var[ValidationResult] = Var(YetToBeValidated)
  
  import FieldValidators._ 
  private val handleUsernameChange = (value: String) => {   
    username.value = value.trim()
    val validationResult = validateUsername(username.value)
    usernameValidation.value = validationResult
    if(validationResult) verifyUsernameAlreadyTaken(username.value)
  } 
  private val handleNameChange = (value: String) => {   
    name.value = value.trim()
    nameValidation.value = validateName(name.value)
  }
  private val handleMessageChange = (value: String) => {   
    message.value = value.trim()
    messageValidation.value = validateMessage(message.value)
  }
  private val handleWhereDidYouHearAboutUsChange = (value: String) => {
    whereDidYouHearAboutUs.value = value
    whereDidYouHearAboutUsValidation.value = validateWhereDidYouHearAboutUs(whereDidYouHearAboutUs.value)
  }
  private val handleSubscribeMeChange = (value: Boolean) => {
    subscribeMe = value
  }
  private val handleAcceptTermsChange = (value: Boolean) => {
    termsAccepted.value = value
    acceptTermsValidation.value = validateAcceptTerms(termsAccepted.value)
  }
  private val handleGenderChange = (value: String) => {
    gender.value = value
    genderValidation.value = validateGender(gender.value)
  }
  private val handleSubscriptionTypeChange = (value: String) => {
    subscriptionType.value = value
    subscriptionTypeValidation.value = validateSubscriptionType(subscriptionType.value)
  } 
  private val handleEmailChange = (value: String) => {
    email.value = value.trim()
    emailValidation.value = validateEmail(email.value)
  }
  private val handlePasswordChange = (value: String) => {
    password.value = value
    passwordValidation.value = validatePassword(password.value)
    // needs to be re-evaluated (for instance if a user types in a password
    // then a matching one, but subsequently changes the original password.
    // this does not need to happen if the field is yet to be validated)
    if(confirmPasswordValidation.value != YetToBeValidated) 
      confirmPasswordValidation.value = validateConfirmPassword(password.value, confirmPassword.value)
  }
  private val handleConfirmPasswordChange = (value: String) => {
    confirmPassword.value = value
    confirmPasswordValidation.value = validateConfirmPassword(password.value, confirmPassword.value)
  }
  
  // async validation
  private def validateUsernameAlreadyTaken(username: String) = {
    usernameValidation.value = getMatchingUsernamesCount().map({
      case 0 => Success("Valid username")
      case 1 => Error(s"Username $username already taken")
      case _ => Success("........") //pending state
    }).getOrElse(Error("Something went wrong"))
  }

  def connectWith() = validateUsernameAlreadyTaken(username.value)

  @dom def build = {
    val form =
      <div>
        <div class={ FIELD }>
          <TextInput label={ "Name" } labelStyle={ Styles.labelStyle } onChange={ handleNameChange }/>
          { renderValidation(nameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <TextInput label={ "Username" } labelStyle={ Styles.labelStyle } onChange={ handleUsernameChange }/>
          { renderValidation(usernameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <EmailInput label={ "Email" } labelStyle={ Styles.labelStyle } onChange={ handleEmailChange } 
          inputValue={ email.value }/>
          { renderValidation(emailValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <PasswordInput label={ "Password" } labelStyle={ Styles.labelStyle } onChange={ handlePasswordChange } 
          inputValue={ password.value }/>
          { renderValidation(passwordValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          { val pwdVal = passwordValidation.bind
            <PasswordInput label={ "Confirm password" } labelStyle={ Styles.labelStyle } 
            onChange={ handleConfirmPasswordChange } 
            inputValue={ confirmPassword.value } isDisabled={ !pwdVal }/>.listen }
          { renderValidation(confirmPasswordValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <RadioInput options={ Seq("Male", "Female") } labelStyle={ Styles.labelStyle } 
          name={ "gender" } onSelect={ handleGenderChange }/>
          { renderValidation(genderValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <RadioInput label={ "Subscription type" } labelStyle={ Styles.labelStyle } 
          options={ Seq("Full", "Trial", "Limited") } name={ "subscription-type" } 
          onSelect={ handleSubscriptionTypeChange }/>
          { renderValidation(subscriptionTypeValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <SelectInput label={ "Where did you hear about us ?" } 
          labelStyle={Styles.labelStyle}
          options={ Seq("News", "Facebook", "Twitter", "Instagram", "Friends") } 
          disabledOptions={ Seq(0, 2) } leftIcon={ <i class="fa fa-globe"/> } 
          onSelect={ handleWhereDidYouHearAboutUsChange }/>
          { renderValidation(whereDidYouHearAboutUsValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <TextareaInput label={ "Additional info" } labelStyle={ Styles.labelStyle } 
          onChange={ handleMessageChange } inputValue={ message.value }/>
          { renderValidation(messageValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <CheckboxInput label="Subscribe me to latest news from GlobeApp" 
          labelStyle={ Styles.labelStyle } 
          onSelect={ handleSubscribeMeChange }/>
        </div>
        <div class={ FIELD }>
          <CheckboxInput label={ "Accept Terms & Conditions" } 
          labelStyle={ Styles.labelStyle }
          onSelect={ handleAcceptTermsChange }/>
          { renderValidation(acceptTermsValidation.bind).bind }
        </div>
        <div class={ getClassName(FIELD, GROUPED) }>
          <div class={ CONTROL }>
            { renderSubmitButton(
                label = "Register",
                isPrimary = true,
                runValidation = runValidation _,
                runSubmit = runSubmit _,
                nameValidation.bind,
                usernameValidation.bind,
                emailValidation.bind,
                messageValidation.bind,
                whereDidYouHearAboutUsValidation.bind,
                genderValidation.bind,
                subscriptionTypeValidation.bind,
                passwordValidation.bind,
                acceptTermsValidation.bind,
                confirmPasswordValidation.bind).bind }
          </div>
        </div>
      </div>

    create(form, "registration-form")
  }

  //room for improvement...run only failing ones
  def runValidation() = {
    handleNameChange(name.value)
    handleUsernameChange(username.value)
    handleEmailChange(email.value)
    handleMessageChange(message.value)
    handleWhereDidYouHearAboutUsChange(whereDidYouHearAboutUs.value)
    handleAcceptTermsChange(termsAccepted.value)
    handleGenderChange(gender.value)
    handlePasswordChange(password.value)
    handleConfirmPasswordChange(confirmPassword.value)
    handleSubscriptionTypeChange(subscriptionType.value)
  }

  def runSubmit() = {
    onSubmit(name.value,
             username.value,
             email.value,
             password.value,
             gender.value)
  }
}

object FieldValidators{
  
  import FormValidators._
  
  def validateName(value: String) = {    
    validateRequiredField(fieldValue = value, fieldName = "Name")
    .|>(validateAlphaNumericField(value, fieldName = "Name"))
    .|>(validateFieldLength(fieldName = "Name", fieldLength = value.length, minLength = 2, maxLength = 10))
  }
  def validateUsername(value: String) = {
    validateRequiredField(fieldValue = value, fieldName = "Username")
    .|>(validateAlphaNumericField(value, fieldName = "Username"))
    .|>(validateFieldLength(fieldName = "Username", fieldLength = value.length, minLength = 2, maxLength = 10))
  }
  def validateMessage(value: String) = { //TODO add remaining chars counter
    validateRequiredField(fieldValue = value, fieldName = "Message")
    .|>(validateFieldLength(fieldName = "Message", fieldLength = value.length, minLength = 20, maxLength = 150))
  }
  def validateWhereDidYouHearAboutUs(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateAcceptTerms(accepted: Boolean) = {
    validateCheckbox(
        checked = accepted, 
        customErrorMessage = Some("Terms & Conditions must be accepted to proceed"))
  }
  def validateGender(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateSubscriptionType(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateEmail(value: String) = {
    validateRequiredField(fieldValue = value, fieldName = "Email")
    .|>(emailIsValid(value))
  }
  def validatePassword(value: String) = { 
    validateRequiredField(fieldValue = value, fieldName = "Password")
    .|>(validateFieldWithRegex(
        fieldValue = value, fieldName = "Password", regexPattern = passwordRegex, 
        customErrorMessage = Some("Invalid charachters: password cannot contain spaces")))
    .|>(validateFieldLength(fieldName = "Password", fieldLength = value.length, minLength = 4, maxLength = 12))
  }
  def validateConfirmPassword(password: String, confirmPassword: String) = {
    validateRequiredField(password, customErrorMessage = Some("Please confirm your password"))
    .|>(validateMatch(password, confirmPassword, "Password"))
  }
}