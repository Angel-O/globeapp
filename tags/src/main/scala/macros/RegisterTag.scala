package macros

import macros.Components.Implicits.ComponentBuilder
import macros.Components.Implicits.CustomTags2
import com.thoughtworks.binding.dom
import macros.Components.Implicits.MyComponentBuilder

object RegisterTag {
  
  //implicit val cst: CustomTags2 = CustomTags2(dom.Runtime.TagsAndTags2)
  
  def reg(custom: dom.Runtime.TagsAndTags2.type, tagName: String, tag: ComponentBuilder) = 
        custom.RegisterTag(params => tag, tagName)
        
  import scala.language.experimental.macros
  def register(custom: dom.Runtime.TagsAndTags2.type, tagName: String, tag: ComponentBuilder): Unit = 
    macro registerTag_impl 
    
  import scala.reflect.macros.blackbox.Context // do I need the blacbox
  def registerTag_impl(c: Context)(custom: c.Expr[dom.Runtime.TagsAndTags2.type],
                              tagName: c.Expr[String], 
                              tag: c.Expr[ComponentBuilder]): c.Expr[Unit] = {
    
    import c.universe._
    (custom, tagName, tag) match {
      case (Expr(custom: dom.Runtime.TagsAndTags2.type), Expr(Literal(Constant(tagNameValue: String))), Expr(tagValue:ComponentBuilder) ) => 
        val result = reg(custom, tagNameValue, tagValue)
        c.Expr(Literal(Constant(result)))
      //case: typeOf
      case (Expr(custom: dom.Runtime.TagsAndTags2.type),Expr(Literal(Constant(tagNameValue: String))),Expr(t)) => {
        println(tagNameValue, t)
//        val tg = tagName.value
//        val t = tag.value
//        val p = params.value
        
        val r = reg(custom, tagNameValue, MyComponentBuilder())
        c.Expr(Literal(Constant(0)))
      }
      case _ => {
        //println(tagName.asInstanceOf[Literal])//, tag, params)
        throw new Exception("tag error: registration failed!")
      }
    }
  } 
  
  import scala.reflect.macros.whitebox.Context
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly
  
  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class tag extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro ???
  }
  
  def impl = {
    
  }
}