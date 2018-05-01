package hoc.form

import components.core.Implicits._
import components.core.ComponentBuilder
import components.Components.{Input, Misc, Layout}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import common.FormValidators.validateRequiredField
import common._, common.FormElements._, common.Styles
import utils.nameOf._

case class CreateMessageDialogBuilder() extends ComponentBuilder {
  def render = this

  var onSubmit: (String) => Unit = _
  var content = ""

  var submitLabel: String = "Send message"

  private val contentValidation: Var[ValidationResult] = Var(YetToBeValidated)

  private val handleContentChange = (value: String) => {
    content = value.trim()
    contentValidation.value = validateRequiredField(content.value)
  }

  @dom def build = {
    <div class={ NOTIFICATION }>
			<div class={ FIELD }>
      		<TextareaInput label={ "Message" } onChange={handleContentChange} inputValue={content}/>
        { renderValidation(contentValidation.bind).bind  }
      </div>
			<div class={ FIELD }>
        { renderSubmitButton(label = submitLabel,
            isPrimary = true,
            runValidation = runValidation _, 
            runSubmit = runSubmit _, 
            contentValidation.bind).bind }
      </div>
    </div>
  }

  def runValidation() = {
    if (!contentValidation.value) handleContentChange(content)
  }

  def runSubmit() = {
    onSubmit(content)
  }
}
