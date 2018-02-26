package views

import com.thoughtworks.binding.dom
import components.Components.Implicits.ComponentBuilder
import scala.language.dynamics
import scala.collection.mutable
//import views.TagsRegistry.CustomTags
import hoc.form.RegistrationFormBuilder
//import components.Components.MyComponentBuilder

object TagsRegistry {
  
  type BuilderFunction = Function0[ComponentBuilder]
  
  implicit val customTags: CustomTags = new CustomTags(dom.Runtime.TagsAndTags2)
  
  implicit final class CustomTags(x: dom.Runtime.TagsAndTags2.type) extends Dynamic{
          
      private val fields = mutable.Map.empty[String, BuilderFunction].withDefault {key => throw new NoSuchFieldError(key)}

      def selectDynamic(key: String) = fields(key).asInstanceOf[Function0[ComponentBuilder]]      

      // TODO check if exists
      def updateDynamic(key: String)(value: BuilderFunction) = fields(key) = value

      def applyDynamic(key: String) = fields(key).asInstanceOf[Function0[ComponentBuilder]]
      
      implicit def RegisterTag(builder: BuilderFunction, tagName: String) = {
        updateDynamic(tagName)(builder)
      }
  }
   
  def register(builder: () => ComponentBuilder, tagName: String) = {
    customTags.RegisterTag(builder, tagName)
  }
}

//TODO move this to tags project
object Tags{
  
  import TagsRegistry. { customTags => cts, register }
  
  // execute once and cache the result
  lazy val customTags = { registerAll() ; cts }
  
  def registerAll() = {
    register(() => new RegistrationFormBuilder(), "RegistrationForm")
  } 
  
  //import macros.RegisterTag.register
  
  //@register
  //val RegistrationForm = "RegistrationForm"
//  @register
//  val MyComponent = "MyComponent"
}