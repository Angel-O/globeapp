package hoc.form

import components.core.Implicits._
import components.core.ComponentBuilder
import components.core.Helpers._
import components.Components.{Input, Misc, Modal}
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

  var onSubmit: (String, String) => Unit = _

  private var title, content = ""
  private var closingDate: LocalDate = _
  private var numberOfOptions: Var[Int] = Var(2)
  private var options = Seq.fill(2)("")
  private val titleValidation, contentValidation, closingDateValidation,
  optionsValidation: Var[ValidationResult] = Var(YetToBeValidated)
  //private val allOptionValidations: Var[Seq[ValidationResult]] = Var(Seq.fill(options.size)(YetToBeValidated))

  private val ovs = Var(OptionsValidationState(Seq.fill(2)(YetToBeValidated)))


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
    //val indexed = allOptionValidations.value.zipWithIndex
    val indexed = ovs.value.validations.zipWithIndex

    val update = indexed.map({case (_, i) => validateRequiredField(options(i))})
    

    //val update = indexed.map({case (_, i) => validateRequiredField(ovs.value.options(i))})

    //allOptionValidations.value = update
    //ovs.value.validations = update
    ovs.value = ovs.value.copy(validations = update)
  }
  private val handleClosingDateChange = (value: String) => {}
  private val handleOptionNumberChange = (value: Int) => {
      //println("Hey")
      //if (value > options.size) {
      if (value > ovs.value.total) {
          println("GR")

          //val optionsToAdd = value - options.size
          val optionsToAdd = value - ovs.value.total
          println("optionsToAdd",optionsToAdd)

          options = options ++ Seq.fill(optionsToAdd)("")
          //ovs.value.options = ovs.value.options ++ Seq.fill(optionsToAdd)("")
          println("options", options)

          //allOptionValidations.value = allOptionValidations.value ++ Seq.fill(optionsToAdd)(YetToBeValidated)
          ovs.value.validations = ovs.value.validations ++ Seq.fill(optionsToAdd)(YetToBeValidated)
      }
      //else if (value <= options.size) {
      else { //if (value <= options.size) {
          println("LS")

          options = options.take(value)
          //ovs.value.options = ovs.value.options.take(value)
          //println("LS-1")
          //println("OPT", options)
          //println("AV", allOptionValidations.value.take(value))

          //allOptionValidations.value = allOptionValidations.value.take(value)
          ovs.value.validations = ovs.value.validations.take(value)

          //println("LS-2")
      }
      //numberOfOptions.value = options.size
      //ovs.value.total = value
      val newOvs = ovs.value.copy(total = value)
      //println("NEWOVS", newOvs)
      ovs.value = newOvs
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
        <div class={ FIELD }>
            <NumericInput label={ "Options" }
                onChange={ handleOptionNumberChange } min={"2"} max={"10"} inputValue={ovs.bind.total.toString} /> //FIX THIS
        </div>
        <div class={ FIELD }>
            { //val validations = allOptionValidations.bind.take(numberOfOptions.bind)

              //val total = numberOfOptions.bind
              //val total = 
              for ((_, i) <- toBindingSeq((0 until ovs.bind.total).zipWithIndex)) yield {
                //allOptionValidations.value = Seq.fill(options.size)(YetToBeValidated)
                //allOptionValidations.value = allOptionValidations.value.take(numberOfOptions.bind)
                //(i)
                //println("OS", options.size)
                //println("INDEx", i)
                val handleOptionChange = (value: String) => { 
                    
                    options = options.updated(i, value)
                    //ovs.value.options = ovs.value.options.updated(i, value)

                    //allOptionValidations.value = allOptionValidations.value.updated(i, validateRequiredField(options(i)))
                    //ovs.value.validations = ovs.value.validations.updated(i, validateRequiredField(options(i)))

                    val newOvs = ovs.value.copy(validations = ovs.value.validations.updated(i, validateRequiredField(options(i))))

                    ovs.value = newOvs

                    println("NEWVAL", newOvs)
                }
                <div>
                    <TextInput label={ s"option ${i + 1}" }
                        onChange={ handleOptionChange } inputValue={options(i)}/>
                    { val all = ovs.bind.validations.take(ovs.value.total)
                      println("ALL", all)
                      renderValidation(all(i)).bind  }
                </div>
            } }
        </div>
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

  def runValidation() = {
    handleTitleChange(title)
    handleContentChange(content)
    //println("VALS", allOptionValidations.value)

    //allOptionValidations.value.zipWithIndex.foreach({case (_, i) => handleOptionsChange(i)})

    handleOptionsChange()
  }

  def runSubmit() = onSubmit(title, content)
}
