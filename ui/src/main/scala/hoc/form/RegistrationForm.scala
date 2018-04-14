package hoc.form

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Helpers._
import components.Components.{ Input, Layout }
import com.thoughtworks.binding.{ dom, Binding }, Binding.Var
import common._, FormElements._, FormValidators._, common.Styles
import appstate.AppCircuit._
import appstate.AuthSelector._
import apimodels.mobile.Genre.{values => appCategories, _}

case class RegistrationFormBuilder() extends ComponentBuilder {
   
  def render = this 
  
  var onSubmit: (String, String, String, String, String, String, String, Boolean) => Unit = _
  var verifyEmailAlreadyTaken: String => Unit = _
  
  private var subscribeMe: Boolean = false // no need to use Var as there is no need to reload (no validation happening)
  private var termsAccepted: Var[Boolean] = Var(false)
  private val name, username, email, message, whereDidYouHearAboutUs, gender, 
  subscriptionType, password, confirmPassword: Var[String] = Var("")
  private val nameValidation, emailValidation, messageValidation, whereDidYouHearAboutUsValidation, 
  genderValidation, usernameValidation, subscriptionTypeValidation, passwordValidation,
  acceptTermsValidation, confirmPasswordValidation: Var[ValidationResult] = Var(YetToBeValidated)
  
  import FieldValidators._ 
  private val handleEmailChange = (value: String) => {   
    email.value = value.trim()
    val validationResult = validateEmail(email.value)
    emailValidation.value = validationResult
    if(validationResult) verifyEmailAlreadyTaken(email.value)
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
  private val handleUsernameChange = (value: String) => {
    username.value = value.trim()
    usernameValidation.value = validateUsername(username.value)
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
  private def validateUserAlreadyRegistered(email: String) = {
    emailValidation.value = getMatchingEmailsCount().map({
      case 0 => Success("Valid email")
      case 1 => Error(s"Email address $email already taken")
      case _ => Success("........") //pending state
    }).getOrElse(Error("Something went wrong"))
  }
  
  connect(validateUserAlreadyRegistered(email.value))(authSelector)

  @dom def build = {
    val form =
      <div>
        <div class={ FIELD }>
          <TextInput label={ "Name" } onChange={ handleNameChange }/>
          { renderValidation(nameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <TextInput label={ "Username" } onChange={ handleUsernameChange }/>
          { renderValidation(usernameValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <EmailInput label={ "Email" } onChange={ handleEmailChange } 
          inputValue={ email.value }/>
          { renderValidation(emailValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <PasswordInput label={ "Password" } onChange={ handlePasswordChange } 
          inputValue={ password.value }/>
          { renderValidation(passwordValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          { val pwdVal = passwordValidation.bind
            <PasswordInput label={ "Confirm password" } 
            onChange={ handleConfirmPasswordChange } 
            inputValue={ confirmPassword.value } isDisabled={ !pwdVal }/>.listen }
          { renderValidation(confirmPasswordValidation.bind).bind }
        </div>
        <div class={ FIELD } style={ "display: flex" }>
        	  <label class={ "label" } style={ "margin-right: 2em" }> Gender </label>
          <RadioInput style={ "margin-right: 2em" } options={ Seq("Male", "Female") } 
          name={ "gender" } onSelect={ handleGenderChange }/>
          { renderValidation(genderValidation.bind).bind }
        </div>
        <div class={ FIELD }> 
        		<label class={ "label" }> Favorite apps </label>
        		<div style={Styles.checkBoxContainer}> { toBindingSeq(appCategories).map(category => 
             <div style={Styles.checkBox}>
          			<CheckboxInput label={ category.toString } 
          				onSelect={ (_:Boolean) => {} }/>
          		</div>)}
        	  </div>
        </div>
        <div class={ FIELD }>
          <SelectInput label={ "Where did you hear about us ?" } 
          options={ Seq("News", "Facebook", "Twitter", "Instagram", "Friends") } 
          disabledOptions={ Seq(0, 2) } leftIcon={ <i class="fa fa-globe"/> } 
          onSelect={ handleWhereDidYouHearAboutUsChange }/>
          { renderValidation(whereDidYouHearAboutUsValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <TextareaInput label={ "Additional info" } 
          onChange={ handleMessageChange } inputValue={ message.value }/>
          { renderValidation(messageValidation.bind).bind }
        </div>
        <div class={ FIELD }>
          <CheckboxInput label="Subscribe me to latest news from GlobeApp" 
          onSelect={ handleSubscribeMeChange }/>
        </div>
        <div class={ FIELD }>
          <CheckboxInput label={ "Accept Terms & Conditions" } 
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

    create(<div><Message header="Registration form" content={ form } /></div>, "registration-form")
  }

  def runValidation() = {
    if(!nameValidation.value) handleNameChange(name.value)
    if(!usernameValidation.value) handleUsernameChange(username.value)
    if(!emailValidation.value) handleEmailChange(email.value)
    if(!messageValidation.value) handleMessageChange(message.value)
    if(!whereDidYouHearAboutUsValidation.value) handleWhereDidYouHearAboutUsChange(whereDidYouHearAboutUs.value)
    if(!acceptTermsValidation.value) handleAcceptTermsChange(termsAccepted.value)
    if(!genderValidation.value) handleGenderChange(gender.value)
    if(!passwordValidation.value) handlePasswordChange(password.value)
    if(!confirmPasswordValidation.value) handleConfirmPasswordChange(confirmPassword.value)
    if(!subscriptionTypeValidation.value) handleSubscriptionTypeChange(subscriptionType.value)
  }

  def runSubmit() = {
    onSubmit(name.value,
             username.value,
             email.value,
             password.value,
             gender.value,
             whereDidYouHearAboutUs.value, 
             message.value, 
             subscribeMe)
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