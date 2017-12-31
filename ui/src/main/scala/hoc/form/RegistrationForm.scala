package hoc.form

import components.Components.Implicits.{ CustomTags2, _ }
import org.scalajs.dom.raw.{ Event, HTMLElement, HTMLImageElement, HTMLButtonElement }
import com.thoughtworks.binding.{ dom, Binding }, Binding.{ Var, Vars, Constants, BindingSeq }
import scala.util.matching.Regex


sealed trait ValidationResult
case class Error(message: String) extends ValidationResult
case class Success(message: String) extends ValidationResult
case object YetToBeValidated extends ValidationResult

case class RegistrationFormBuilder() extends ComponentBuilder {
   
  def render = this 
  
  private var subscribeMe: Boolean = false // no need to use Var as there is no need to reload (no validation happening)
  private var termsAccepted: Var[Boolean] = Var(false)
  private val name, email, message, whereDidYouHearAboutUs, gender, 
  subscriptionType, password, confirmPassword: Var[String] = Var("")
  private val nameValidation, emailValidation, messageValidation, whereDidYouHearAboutUsValidation, 
  genderValidation, subscriptionTypeValidation, passwordValidation,
  acceptTermsValidation, confirmPasswordValidation: Var[ValidationResult] = Var(YetToBeValidated)
  
  import FieldValidators._ 
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
  
  @dom def build = {

    // using a form tag rather than a div allows for page reload on submit (and autocompletion)...change as needed
    val form =
      <form>
				<div class={FIELD}>
        	<TextInput
      			label={"Name"} 			
    				onChange={handleNameChange}
    				inputValue={name.value}/>
					 { renderValidation(nameValidation.bind).bind }	
				</div>
				<div class={FIELD}>
        	<EmailInput
      			label={"Email"} 			
    				onChange={handleEmailChange}
    				inputValue={email.value}/>
					 { renderValidation(emailValidation.bind).bind }	
				</div>
				<div class={FIELD}>
        	<PasswordInput
      			label={"Password"} 			
    				onChange={handlePasswordChange}
    				inputValue={password.value}/>
					 { renderValidation(passwordValidation.bind).bind }	
				</div>
				<div class={FIELD}>
				  { val pwd = password.bind
				    <PasswordInput
      				label={"Confirm password"} 			
    					onChange={handleConfirmPasswordChange}
    					inputValue={confirmPassword.value}
    					isDisabled={pwd.isEmpty}/>.listen }
					 { renderValidation(confirmPasswordValidation.bind).bind }	
				</div>
				<div class={FIELD}>
					<RadioInput
						options={Seq("Male", "Female")}
						name={"gender"}
						onSelect={handleGenderChange}/>
          { renderValidation(genderValidation.bind).bind }	
				</div>
				<div class={FIELD}>
					<RadioInput
						label={"Subscription type"}
						options={Seq("Full", "Trial", "Limited")}
						name={"subscription-type"}
						onSelect={handleSubscriptionTypeChange}/>
					{ renderValidation(subscriptionTypeValidation.bind).bind }	
				</div>
				<div class={FIELD}>
					<SelectInput
						label={"Where did you hear about us ?"}
      			options={Seq("News","Facebook", "Twitter", "Instagram", "Friends")}
      			disabledOptions={Seq(0, 2)}
						leftIcon={<i class="fa fa-globe"/>}
						onSelect={handleWhereDidYouHearAboutUsChange}/>
          { renderValidation(whereDidYouHearAboutUsValidation.bind).bind }	
				</div>
				<div class={FIELD}>
        	<TextareaInput 
						label={ "Additional info" }
						onChange={handleMessageChange}
						inputValue={message.value}/>
					{ renderValidation(messageValidation.bind).bind }	
				</div>
				<div class={FIELD}>
					<CheckboxInput
						label="Subscribe me to latest news from App Globe"
						onSelect={handleSubscribeMeChange}/>
				</div>
				<div class={FIELD}>
					<CheckboxInput
						label={"Accept Terms & Conditions"}
						onSelect={handleAcceptTermsChange}/>
					{ renderValidation(acceptTermsValidation.bind).bind }	
				</div>
				<div class="field is-grouped">
  				<div class={CONTROL}>
            { renderSubmitButton(
                nameValidation.bind,
                emailValidation.bind,
                messageValidation.bind, 
                whereDidYouHearAboutUsValidation.bind,
                acceptTermsValidation.bind,
                genderValidation.bind,
                passwordValidation.bind,
                confirmPasswordValidation.bind,
                subscriptionTypeValidation.bind).bind }
          </div>
				</div>
      </form>.asInstanceOf[HTMLElement]
      
		form
  }
  
  @dom def renderSubmitButton(results: ValidationResult*) = { 
    
    val inError = results.exists(_.isInstanceOf[Error])
    val notFullyValidated = results.exists(_ == YetToBeValidated)
    
    //room for improvement...run only failing ones
    def runValidation() = {
      handleNameChange(name.value)
      handleEmailChange(email.value)
      handleMessageChange(message.value)
      handleWhereDidYouHearAboutUsChange(whereDidYouHearAboutUs.value)
      handleAcceptTermsChange(termsAccepted.value)
      handleGenderChange(gender.value)
      handlePasswordChange(password.value)
      handleConfirmPasswordChange(confirmPassword.value)
      handleSubscriptionTypeChange(subscriptionType.value)
    }
    
    //@noinline //do I need this annotation?? investigate fastOPTJS is being slow...
    def handleSubmit() = notFullyValidated match{
      case true => runValidation()
      case _ => println(
          s"""SUBMITTING...
              name: ${name.value}
              email: ${email.value}
              message: ${message.value}
              where did you hear about us? : ${whereDidYouHearAboutUs.value}
              gender: ${gender.value}
              subscription: ${subscriptionType.value}
              subscribe me: $subscribeMe
              password: ${password.value}
              confirm password: ${confirmPassword.value}
              T&C accepted: ${termsAccepted.value}""")
    }
      
    val submitButton = <Button 
												label="Register" 
												isPrimary={true}
      									onClick={handleSubmit _}  
												isDisabled={inError}/>
		submitButton
  }
  
  @dom def renderValidation(result: ValidationResult) = {
    
    val (errorMsg, successMsg) = result match{
      case Error(msg) => (msg, "")
      case Success(msg) => ("", msg)
      case YetToBeValidated => ("", "")
    }
    
    <FieldValidation errorMessage={errorMsg} successMessage={successMsg}/>	
  }
}

object FieldValidators{
  def validateName(value: String) = {    
    val requiredValidationResult = validateRequiredField(fieldValue = value, fieldName = "Name")
    requiredValidationResult match {
      case Error(_) | YetToBeValidated => requiredValidationResult
      case Success(_) => {
        def isValid = (ch: Char) => ch.isLetter || ch == ' ' //TODO allow only one space...
        if(!value.forall(isValid)) 
          Error("Name can only contain letters")
        else
          validateFieldLength(fieldName = "Name", fieldValue = value, minLength = 2, maxLength = 10)
      }
    }
  }
  def validateMessage(value: String) = { //TODO add remaining chars counter
    validateFieldLength(fieldName = "Message", fieldValue = value, minLength = 20, maxLength = 150)
  }
  def validateWhereDidYouHearAboutUs(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateAcceptTerms(accepted: Boolean) = {
    if(accepted) Success("") else Error("Terms & Conditions must be accepted to proceed")
  }
  def validateGender(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateSubscriptionType(value: String) = {
    validateRequiredField(fieldValue = value)
  }
  def validateEmail(value: String) = {
    val requiredValidationResult = validateRequiredField(fieldValue = value, fieldName = "Email")
    requiredValidationResult match {
      case Error(_) | YetToBeValidated => requiredValidationResult
      case Success(_) => EmailValidator.isValid(value) match {
        case true => Success("Valid email")
        case false => Error("Please provide a valid email address")  
      }
    }
  }
  def validatePassword(value: String) = {
    val requiredValidationResult = validateRequiredField(fieldValue = value, fieldName = "Password")
    requiredValidationResult match {
      case Error(_) | YetToBeValidated => requiredValidationResult
      case Success(_) => {
        val lengthValidationResult = validateFieldLength(fieldName = "Password", fieldValue = value, minLength = 4, maxLength = 12) 
        lengthValidationResult match {
          case Error(_)  => lengthValidationResult
          case _ => {
            //TODO extract validation to separate class
            val passwordRegex = "(^[a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)".r 
            validateFieldWithRegex(fieldValue = value, fieldName = "Password", regexPattern = passwordRegex)
          }
        }
      }
    }
  }
  def validateConfirmPassword(password: String, confirmPassword: String) = {
    if(confirmPassword.isEmpty){
      Error("Please confirm your password")
    }
    else { 
      password == confirmPassword match {
        case true => Success("Passwords match")
        case false => Error("Password doesn't match")
      }
    }
  }
  
  private def validateFieldWithRegex(fieldValue: String, fieldName: String, regexPattern: Regex) = fieldValue match {
    case regexPattern(_) => Success(s"Valid ${fieldName.toLowerCase}")
    case _ => Error(s"Please provide a valid ${fieldName.toLowerCase}") //TODO provide more useful error message
  }
  
  private def validateRequiredField(fieldValue: String, fieldName: String = "") = {
    if(fieldValue.isEmpty) Error(s"$fieldName ${if(fieldName.isEmpty)"F"else"f"}ield is required")
    else Success("")
  }
  
  private def validateFieldLength(fieldName: String, fieldValue: String, minLength: Int, maxLength: Int) = {
    val result = fieldValue.length match {
      case 0 => Error(s"$fieldName field cannot be empty") //TODO remove this...use required validation instead     
      case num => {
        if (num > maxLength) Error(s"$fieldName field cannot exceed $maxLength characters")
        else if (num < minLength) Error(s"$fieldName field must be at least $minLength characters long")
        else Success(s"$fieldName field is valid")
      }
    }
    
    result
  }
}