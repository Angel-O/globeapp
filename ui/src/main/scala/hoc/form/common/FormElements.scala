package hoc.form.common

import com.thoughtworks.binding.dom
import components.core.Implicits._
import components.core.ComponentBuilder
import components.Components.CustomTags2

object FormElements {
  def renderSubmitButton(label: String,
                         isPrimary: Boolean,
                         runValidation: () => Unit,
                         runSubmit: () => Unit,
                         results: ValidationResult*) = {

    val inError = results.exists(_.isError)
    val notFullyValidated = results.exists(_.isNotValidated)

    def handleSubmit() =
      if (notFullyValidated) runValidation() else runSubmit()

    @dom
    val button =
      <Button 
          label={label} 
          isPrimary={isPrimary}
          onClick={handleSubmit _}
          isDisabled={inError}/>

    button
  }

  def renderValidation(result: ValidationResult) = {

    val (errorMsg, successMsg) = result match {
      case Error(msg)       => (msg, "")
      case Success(msg)     => ("", msg)
      case YetToBeValidated => ("", "")
    }

    @dom
    val validation =
      <FieldValidation errorMessage={errorMsg} successMessage={successMsg}/>

    validation
  }
}
