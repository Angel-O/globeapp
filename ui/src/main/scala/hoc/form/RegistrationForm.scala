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

case class RegistrationFormBuilder() extends ComponentBuilder {
   
  def render = this 
  
  var onSubmit: () => Unit = _
  
  private var subscribeMe: Boolean = false // no need to use Var as there is no need to reload (no validation happening)
  private var termsAccepted: Var[Boolean] = Var(false)
  private val name, email, message, whereDidYouHearAboutUs, gender, 
  subscriptionType, password, confirmPassword: Var[String] = Var("")
  private val nameValidation, emailValidation, messageValidation, whereDidYouHearAboutUsValidation, 
  genderValidation, subscriptionTypeValidation, passwordValidation,
  acceptTermsValidation, confirmPasswordValidation: Var[ValidationResult] = Var(YetToBeValidated)
  
  import FieldValidators._ 
  
  import scala.util.{Success => Ok, Failure}
  import scalajs.js
  private val handleNameChange = (value: String) => {   
    name.value = value.trim()
    validateUserNameAlreadyTaken(value).onComplete{
        case Ok(result) => nameValidation.value = validateName(name.value)|>result
        case Failure(x) => nameValidation.value = validateName(name.value); //x.printStackTrace()
    }   
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
    				onChange={handleNameChange}/>
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
				  { val pwdVal = passwordValidation.bind
				    <PasswordInput
      				label={"Confirm password"} 			
    					onChange={handleConfirmPasswordChange}
    					inputValue={confirmPassword.value}
    					isDisabled={!pwdVal.isInstanceOf[Success]}/>.listen }
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
						label="Subscribe me to latest news from GlobeApp"
						onSelect={handleSubscribeMeChange}/>
				</div>
				<div class={FIELD}>
					<CheckboxInput
						label={"Accept Terms & Conditions"}
						onSelect={handleAcceptTermsChange}/>
					{ renderValidation(acceptTermsValidation.bind).bind }	
				</div>
				<div class={getClassName(FIELD, GROUPED)}>
  				<div class={CONTROL}>
            { renderSubmitButton(
                nameValidation.bind, 
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
      </form>.asInstanceOf[HTMLElement]
           
    create(form, "registration-form").bind.asInstanceOf[HTMLElement]
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
      case _ => {
        println(
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
              // Note components can have dynamic fields!! just refer to them by appending the this qualifier
              // (e.g. this.onClick) and convert them to the right type
              this.onClick()
              onSubmit()
              }
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
  
  import FormValidators._
  val passwordRegex = "(^[a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)".r
  
  
  //import apimodels.User
  import apimodels.User
  def validateUserNameAlreadyTaken(userName: String) = {
//    val future = FutureBinding(Ajax.get(
//      url = "http://localhost:9000/users", 
//      data = null, 
//      timeout = 9000, 
//      headers = Map.empty, 
//      withCredentials = false, 
//      responseType = "application/json"))
//    
//    @dom def renderFuture = {
//      val arrived = future.bind match {
//        case Some(Ok(xhr)) => {
//          val users = js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[User]].toSeq         
//          users
//        }
//        case Some(Failure(error)) => Seq.empty//s"$error"
//      }     
//      arrived
//    }
    
    val future = Ajax.get(
      url = "http://localhost:9000/api/users", 
      data = null, 
      timeout = 9000, 
      headers = Map.empty, 
      withCredentials = false, 
      responseType = "text")
      
      import org.scalajs.dom.console
    
      future.map { xhr => 
          //val users = js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[User]].toSeq
        val us = js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[js.Dynamic]].toSeq
        console.warn("Users, ", us.head.isInstanceOf[User])
        
        //val kk = J
        
          val users = js.JSON.parse(xhr.responseText).asInstanceOf[js.Array[User]].toSeq   
          users.foreach(x => console.warn("user:", x.name))
          //println("Users", users)
          users.exists(_.name.toString == userName) match{
            case true => Error(s"$userName already taken")
            case _ => Success("Valid username")
          }
      }  
  }
  
  
  def validateName(value: String) = {    
    validateRequiredField(fieldValue = value, fieldName = "Name")
    .|>(validateAlphaNumericField(value, fieldName = "Name"))
    .|>(validateFieldLength(fieldName = "Name", fieldLength = value.length, minLength = 2, maxLength = 10))
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