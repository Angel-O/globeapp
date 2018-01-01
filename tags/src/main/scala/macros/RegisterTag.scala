package macros

import macros.Components.Implicits.ComponentBuilder
import macros.Components.Implicits.CustomTags2
import com.thoughtworks.binding.dom
import macros.Components.Implicits.MyComponentBuilder

object RegisterTag {
  
  //implicit val cst: CustomTags2 = CustomTags2(dom.Runtime.TagsAndTags2)
  
  def reg(custom: dom.Runtime.TagsAndTags2.type, tagName: String, tag: ComponentBuilder, params: Seq[Any]) = 
        custom.RegisterTag(params => tag, tagName)
        
  import scala.language.experimental.macros
  def register(custom: dom.Runtime.TagsAndTags2.type, tagName: String, tag: ComponentBuilder, params: Seq[Any]): Unit = 
    macro registerTag 
    
  import scala.reflect.macros.blackbox.Context
  def registerTag(c: Context)(custom: c.Expr[dom.Runtime.TagsAndTags2.type],
                              tagName: c.Expr[String], 
                              tag: c.Expr[ComponentBuilder], 
                              params: c.Expr[Seq[Any]]): c.Expr[Unit] = {
    
    import c.universe._
    (custom, tagName, tag, params) match {
      case (Expr(custom: dom.Runtime.TagsAndTags2.type), Expr(Literal(Constant(tagNameValue: String))), Expr(tagValue:ComponentBuilder), Expr(paramsValue:Seq[Any]) ) => 
        val result = reg(custom, tagNameValue, tagValue, paramsValue)
        c.Expr(Literal(Constant(result)))
      //case: typeOf
      case (Expr(custom: dom.Runtime.TagsAndTags2.type),Expr(Literal(Constant(tagNameValue: String))),Expr(t),Expr(p)) => {
        println(tagNameValue,t,p)
//        val tg = tagName.value
//        val t = tag.value
//        val p = params.value
        val r = reg(custom, tagNameValue, MyComponentBuilder(), Seq.empty)
        c.Expr(Literal(Constant(0)))
      }
      case _ => {
        //println(tagName.asInstanceOf[Literal])//, tag, params)
        throw new Exception("tag error: registration failed!")
      }
    }
  }                            
}