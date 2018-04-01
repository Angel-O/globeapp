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

case class OptionsValidationState(var validations: Seq[ValidationResult], var total: Int = 2)

case class CreatePollFormBuilder() extends ComponentBuilder {

  def render = this

  var onSubmit: (String, String, LocalDate, Seq[String]) => Unit = _

  private var title, content = ""
  private var closingDate: LocalDate = _
  private var numberOfOptions: Var[Int] = Var(2)
  private var options = Seq.fill(2)("")
  private val titleValidation, contentValidation, closingDateValidation,
  optionsValidation: Var[ValidationResult] = Var(YetToBeValidated)
  //private val allOptionValidations: Var[Seq[ValidationResult]] = Var(Seq.fill(options.size)(YetToBeValidated))

  private val ovs = Var(OptionsValidationState(Seq.fill(2)(YetToBeValidated))) //TODO rmove this now...


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
    val update = indexed.map({case (_, i) => validateRequiredField(options(i))})
    ovs.value = ovs.value.copy(validations = update)
  }
  private val handleClosingDateChange = (value: String) => {
      closingDate = if(value.trim == "") null else LocalDate.parse(value)
      closingDateValidation.value = validateRequiredField(
      value,
      "closing date",
      Some(s"Please provide a closing date"))
  }
  private val handleOptionNumberChange = (value: Int) => {
      val changeOccurred = value != option.size
      if (value > options.size) {
          val optionsToAdd = value - ovs.value.total
          options = options ++ Seq.fill(optionsToAdd)("")
          ovs.value.validations = ovs.value.validations ++ Seq.fill(optionsToAdd)(YetToBeValidated)
      }
      else if (value < options.size) {
          options = options.take(value)
          ovs.value.validations = ovs.value.validations.take(value)
      }
      if (changeOccurred){ numberOfOptions.value = value }
  }
  @dom def build = {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    val today = LocalDate.parse(new Date().toLocaleDateString, formatter) //LocalDate.parse("2007-12-12")
    val days = { for { i <- 1 to 10 } yield (today.plusDays(i)) } :+ today map (_.toString) //TODO remove today

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
                onChange={ handleOptionNumberChange } min={"2"} max={"10"} inputValue={ovs.bind.total.toString} /> //FIX THIS
            </div>,
            <div> { for ((_, i) <- toBindingSeq((0 until numberOfOptions.bind).zipWithIndex)) yield {    
                val handleOptionChange = (value: String) => { 
                    options = options.updated(i, value)
                    val newOvs = ovs.value.copy(validations = ovs.value.validations.updated(i, validateRequiredField(options(i))))
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

  @dom def createValidation(index: Int) = {
      val all = ovs.bind.validations.take(numberOfOptions.bind)
      <div>{ renderValidation(all(index)).bind }</div> //wrapping inside div necessary double nested binding dependency!!! 
  }

  def runValidation() = {
    handleTitleChange(title)
    handleContentChange(content)
    handleClosingDateChange(closingDate match {case null => "" case _ => closingDate.toString})
    handleOptionsChange()
  }

  def runSubmit() = onSubmit(title, content, closingDate, options)
}
