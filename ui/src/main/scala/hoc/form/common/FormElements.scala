package hoc.form.common

import com.thoughtworks.binding.dom
import components.Components.Implicits.{CustomTags2, ComponentBuilder, _}
import hoc.form._ //TODO organize packages better

object FormElements {
  @dom def renderSubmitButton(label: String,
                              isPrimary: Boolean,
                              runValidation: () => Unit,
                              runSubmit: () => Unit,
                              results: ValidationResult*) = {

    val inError = results.exists(_.isError)
    val notFullyValidated = results.exists(_.isNotValidated)

    def handleSubmit() =
      if (notFullyValidated) runValidation() else runSubmit()

    <Button 
        label={label} 
        isPrimary={isPrimary}
        onClick={handleSubmit _}
        isDisabled={inError}/>
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
