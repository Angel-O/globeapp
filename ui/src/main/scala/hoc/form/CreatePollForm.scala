package hoc.form

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Helpers._
import components.Components.{Input, Misc, Modal, Layout}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var, Binding.Vars
import appstate.AuthSelector
import common.FormValidators.validateRequiredField
import common._, common.FormElements._, common.Styles
import utils.nameOf._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.scalajs.js.Date

case class OptionsValidationState(var validations: Seq[ValidationResult])

case class CreatePollFormBuilder() extends ComponentBuilder {

  def render = this

  var onSubmit: (String, String, LocalDate, Seq[String]) => Unit = _

  private var title, content = ""
  private var closingDate: LocalDate = _
  private var numberOfOptions: Var[Int] = Var(2)
  private var options = Seq.fill(2)("")
  private val titleValidation, contentValidation, closingDateValidation: Var[ValidationResult] = Var(YetToBeValidated)

  //TODO remove this once a better solution is found and restore allOptionValidations
  private val ovs = Var(OptionsValidationState(Seq.fill(2)(YetToBeValidated)))
  //private val allOptionValidations: Var[Seq[ValidationResult]] = Var(Seq.fill(options.size)(YetToBeValidated))

  private val handleTitleChange = (value: String) => {
    title = value.trim()
    titleValidation.value = validateRequiredField(
      title,
      nameOf(title),
      Some(s"Please provide a ${nameOf(title)}"))
  }
  private val handleContentChange = (value: String) => {
    content = value
    contentValidation.value = validateRequiredField(
      content,
      nameOf(content),
      Some(s"Please provide a ${nameOf(content)}"))
  }
  private val handleOptionsChange = () => {
    val indexed = ovs.value.validations.zipWithIndex
    val update = indexed.map({ case (_, i) => validateRequiredField(options(i)) |> validateUniqueOption(options(i), i) })
    ovs.value = ovs.value.copy(validations = update)
  }
  private val handleClosingDateChange = (value: String) => {
    closingDate = if (value.trim == "") null else LocalDate.parse(value)
    closingDateValidation.value = validateRequiredField(
      value,
      "closing date",
      Some(s"Please provide a closing date"))
  }
  // Note: interesting, by wrapping the validation in the ovs case class we can update
  // the value without triggering the binding update. This avoids a situation where
  // two updates happening in the same method (this method. The first update is the
  // nuberOfOptions the second update is "masked" by the ovs: rather than calling the
  // value method on the ovs the update is performed on the validations field of
  // the ovs) could cause issues (See notes on createValidation method)
  private val handleOptionNumberChange = (value: Int) => {
    val changeOccurred = value != options.size
    if (value > options.size) {
      val optionsToAdd = value - options.size
      options = options ++ Seq.fill(optionsToAdd)("")
      ovs.value.validations = ovs.value.validations ++ Seq.fill(optionsToAdd)(YetToBeValidated)
    } else if (value < options.size) {
      options = options.take(value)
      ovs.value.validations = ovs.value.validations.take(value)
    }
    if (changeOccurred) { numberOfOptions.value = value }
  }
  @dom def build = {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    val today = LocalDate.parse(new Date().toLocaleDateString, formatter) 
    val days = { for { i <- 1 to 10 } yield (today.plusDays(i)) } map (_.toString) //TODO remove today

    val form =
      <div>
        <div class={ FIELD }>
            <TextInput label={ "Title" }
                onChange={ handleTitleChange } />
            { renderValidation(titleValidation.bind).bind }
        </div>
        <div class={ FIELD }>
            <TextareaInput label={ "Content" } 
                onChange={ handleContentChange } />
            { renderValidation(contentValidation.bind).bind }
        </div>
        <div class={ FIELD }>
            <SelectInput label={ "Closing date" } onSelect={ value: String => handleClosingDateChange(value) } 
                hasDefaultOption={true} defaultOptionText={""} leftIcon={ <Icon id="calendar"/>.build.bind } options={ days }/>
            { renderValidation(closingDateValidation.bind).bind  }
        </div>
        <Box sizes={Seq(`1/4`)} contents={Seq( 
            <div class="field-label">
                <label class="label">Options</label>
                <NumericInput 
                onChange={ handleOptionNumberChange } min={"2"} max={"10"} inputValue={options.size.toString} /> //FIX THIS
            </div>,
            <div> { for ((_, i) <- toBindingSeq((0 until numberOfOptions.bind).zipWithIndex)) yield {    
                val handleOptionChange = (value: String) => { 
                    options = options.updated(i, value)
                    val newOvs = ovs.value.copy(
                        validations = ovs.value.validations.updated(
                            i, validateRequiredField(options(i)) |> validateUniqueOption(options(i), i) ))
                    ovs.value = newOvs
                }
                <div class={ FIELD } style={"display: flex; flex-direction: column"}>
                    <TextInput label={ s"option ${i + 1}" }
                        onChange={ handleOptionChange } inputValue={options(i)}/>
                    { createValidation(i).bind.bind } <!-- double bind necessary!!! nested binding dependency...JEEZ --> 
                </div> }}
            </div>
        )}/>
        <div>
          { renderSubmitButton(label = "Create poll",
                       isPrimary = true,
                       runValidation = runValidation _,
                       runSubmit = runSubmit _,
                       titleValidation.bind +: 
                       contentValidation.bind +: 
                       closingDateValidation.bind +:
                       ovs.bind.validations.bind: _*).bind }
        </div> 
      </div>

    create(form, "poll-form")
  }

  // This method depends on two bindings: numberOfOptions and ovs.
  // The updates happening chcange the size of the validation collection: 
  // this could be problematic if the updates happen together
  // as an index out of boud can be raised: (the index passed
  // to the method can be out of sync with one of the updates...)
  // (See comments on handleOptionNumberChange method) to understand how 
  // the issue is resolved)
  @dom def createValidation(index: Int) = {
    val all = ovs.bind.validations.take(numberOfOptions.bind)
    <div>{ renderValidation(all(index)).bind }</div> //wrapping inside div necessary double nested binding dependency!!!
  }

  def runValidation() = {
    handleTitleChange(title)
    handleContentChange(content)
    handleClosingDateChange(Option(closingDate).map(_.toString).getOrElse(""))
    handleOptionsChange()
  }

  def runSubmit() = onSubmit(title, content, closingDate, options)

  private def validateUniqueOption(currentOption: String, currentOptionIndex: Int) = {
    options
      .zipWithIndex
      .filter({ case (_, i) => i != currentOptionIndex })
      .find({ case (option, _) => option.trim != "" && option.trim == currentOption })
      .map({ case (_, i) => Error(s"This option was already added as option ${i + 1}") })
      .getOrElse(Success(""))
  }
}
