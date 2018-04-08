package hoc.form

import components.core.Implicits._
import components.core.ComponentBuilder
import components.Components.{Input, Misc, Layout}
import com.thoughtworks.binding.{dom, Binding}, Binding.Var
import common.FormValidators.validateRequiredField
import common._, common.FormElements._, common.Styles
import utils.nameOf._

case class CreateReviewFormBuilder() extends ComponentBuilder {
  def render = this

  var onSubmit: (String, String, Int) => Unit = _
  var title, content = ""
  var rating: Int = _
  
  var submitLabel: String = "Add review"

  private lazy val titleValidation = initValidation(title == "")
  private lazy val contentValidation = initValidation(content == "")
  private lazy val ratingValidation = initValidation(rating == 0)

  private def initValidation(condition: Boolean): Var[ValidationResult] = {
    if (condition) Var(YetToBeValidated) else Var(Success(""))
  }

  private val handleContentChange = (value: String) => {
    content = value.trim()
    contentValidation.value = validateRequiredField(content.value)
  }

  private val handleTitleChange = (value: String) => {
    title = value.trim()
    titleValidation.value = validateRequiredField(title.value)
  }

  private val handleRatingChange = (value: Int) => {
    rating = value
    ratingValidation.value = validateRequiredField(
      if (rating == 0) "" else rating.toString)
  }

  @dom def build = {
    <div class={ NOTIFICATION }>
    		<Box sizes={Seq(`2/3`)} contents={Seq(
    		    <div>
    		    	<TextInput label= { "Title" } onChange={handleTitleChange} inputValue={title}/>
    		    	{ renderValidation(titleValidation.bind).bind  }
    		    </div>,
    		    <div>
    		    	<SelectInput label={ "Rating" } onSelect={ value: String => handleRatingChange(value.toInt) } 
                hasDefaultOption={true} defaultOptionText={""} leftIcon={ <Icon id="star"/>.build.bind } 
                selectedOptions={Seq(rating - 1)} options={ Seq("1", "2", "3", "4", "5") }/>
              { renderValidation(ratingValidation.bind).bind  }
    				</div>
    		)}/>  			
			<div class={ FIELD }>
      		<TextareaInput label={ "Content" } onChange={handleContentChange} inputValue={content}/>
        { renderValidation(contentValidation.bind).bind  }
      </div>
			<div class={ FIELD }>
        { renderSubmitButton(label = submitLabel,
            isPrimary = true,
            runValidation = runValidation _, 
            runSubmit = runSubmit _, 
            titleValidation.bind, 
            contentValidation.bind, 
            ratingValidation.bind).bind }
      </div>
    </div>
  }

  def runValidation() = {
    if (!contentValidation.value) handleContentChange(content)
    if (!titleValidation.value) handleTitleChange(title)
    if (!ratingValidation.value) handleRatingChange(rating)
  }

  def runSubmit() = {
    onSubmit(title, content, rating)
  }
}
