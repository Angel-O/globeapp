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
  
  var onSubmit: () => Unit = _
  
  private val contentValidation, titleValidation: Var[ValidationResult] = Var(YetToBeValidated)
  private var content, title = ""

  private val handleContentChange = (value: String) => {
    content = value.trim()
    contentValidation.value = validateRequiredField(value)
  }
  
  private val handleTitleChange = (value: String) => {
    title = value.trim()
    titleValidation.value = validateRequiredField(value)
  }
  
  @dom def build = {
    <div class={ NOTIFICATION }>
    		<Box sizes={Seq(`2/3`)} contents={Seq(
    		    <div>
    		    		<TextInput label= { "Title" } onChange={handleTitleChange}/>
    		    		{ renderValidation(titleValidation.bind).bind  }
    		    	</div>,
    		    <div>
    		    		<SelectInput label={ "Rating" } onSelect={ _:String => println("Hi") } hasDefaultOption={false}
    					options={ Seq("1", "2", "3", "4", "5") } leftIcon={ <Icon id="star"/>.build.bind } />
    				</div>
    		)}/>  			
			<div class={ FIELD }>
      		<TextareaInput label={ "Content" } onChange={handleContentChange}/>
        { renderValidation(contentValidation.bind).bind  }
      </div>
			<div class={ FIELD }>
        { renderSubmitButton(label = "Add review",
            isPrimary = true,
            runValidation = runValidation _, 
            runSubmit = onSubmit, contentValidation.bind).bind }
      </div>
    </div>
  }
  
  def runValidation() = {
    if(!contentValidation.value) handleContentChange(content)
    if(!titleValidation.value) handleTitleChange(title)
  }
}