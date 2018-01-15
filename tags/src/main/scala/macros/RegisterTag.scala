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
  def register2(custom: dom.Runtime.TagsAndTags2.type, tagName: String, tag: ComponentBuilder): Unit = 
    macro registerTag_impl2
    
  def register(custom: dom.Runtime.TagsAndTags2.type, tagName: String, component: Any): Any = 
    macro registerTag_impl 
    
  def registerTag_impl(c: scala.reflect.macros.blackbox.Context)(custom: c.Tree, tagName: c.Tree, component: c.Tree) = {
    import c.universe._
    //val q"..$stats" = x
    //println(stats)
    
    //throw new Exception(s"STARTING OUT!: $component")
    
//    val y = q""" val cst = new CustomTags2(dom.Runtime.TagsAndTags2); 
//        val params: Seq[Any] = Seq.empty
//        cst.RegisterTag(params => $component.asInstanceOf[ComponentBuilder], $tagName)"""
//        
//    //c.reifyTree(c, c, tree)
//        c.Expr(y)
//        
//    val z = q"""implicit val num = 3"""
//    
//    c.Expr(z)
//    
//    val r = q"""
//            object ff{
//              final class CustomTags3(x: dom.Runtime.TagsAndTags2.type){
//                def MyComponent() = new MyComponentBuilder()
//              }
//            }
//            import ff._"""
//    
//    c.Expr(r)
    
    val yes = q"""
                 val params = Seq.empty
                 $custom.RegisterTag(params => new MyComponentBuilder(), "MyComponent")
              """
    c.Expr(yes)
    
    val no = q"""
            
              final class CustomTags3(x: dom.Runtime.TagsAndTags2.type){
                def MyComponent() = new MyComponentBuilder()
              }
            
            new CustomTags3($custom)"""
    
    c.Expr(no)
    //Literal(Constant(no))
  }
   
  import scala.reflect.macros.blackbox.Context
  object debug {
  def apply[T](x: => T): T = macro impl
  def impl(c: Context)(x: c.Tree) = { 
    import c.universe._
    val q"..$stats" = x
    val loggedStats = stats.flatMap { stat =>
      val msg = "executing " + showCode(stat)
      List(q"println($msg)", stat)
    }
    q"..$loggedStats"
  }
}  
    
  import scala.reflect.macros.blackbox.Context // do I need the blackbox
  def registerTag_impl2(c: Context)(custom: c.Expr[dom.Runtime.TagsAndTags2.type],
                              tagName: c.Expr[String], 
                              tag: c.Expr[ComponentBuilder]): c.Expr[Unit] = {
    
    import c.universe._
    (custom, tagName, tag) match {
      case (Expr(custom: dom.Runtime.TagsAndTags2.type), Expr(Literal(Constant(tagNameValue: String))), Expr(tagValue:ComponentBuilder) ) => 
        val result = reg(custom, tagNameValue, tagValue)
        println("RESULT:", result)
        c.Expr(Literal(Constant(result)))
      //case: typeOf
      case (Expr(custom: dom.Runtime.TagsAndTags2.type),Expr(Literal(Constant(tagNameValue: String))),Expr(t)) => {
        println(tagNameValue, t)
//        val tg = tagName.value
//        val t = tag.value
//        val p = params.value
        
        val r = reg(custom, tagNameValue, MyComponentBuilder())
        println("HI")
        c.Expr(Literal(Constant(0)))
        throw new Exception(s"tag error??????: registration failed!")
      }
      case (a: Expr[_], b, c) => {
        //println(tagName.asInstanceOf[Literal])//, tag, params)
        throw new Exception(s"""tag error: registration failed! ${a.actualType}, $b, $c""")
      }
    }
  }
  
  import scala.reflect.macros.whitebox.{Context => theC}
  import scala.language.experimental.macros
  import scala.annotation.StaticAnnotation
  import scala.annotation.compileTimeOnly
  
  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class tag extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro tag.impl
  }
  
  object tag {
    def impl(c: theC)(annottees: c.Expr[Any]*) = {
    import c.universe._
    val inputs: List[Tree] = annottees.map(_.tree).toList
    
    val tree = inputs(0)   
    
    val q"""
        $mods class $tpname[..$tparams] $ctorMods(...$paramss) 
        extends { ..$earlydefns } with ..$parents { $self => ..$stats }
        """ = tree
     
      val tagName = TermName(tpname.toString().replace("Builder", ""))
      val objectName = TermName(tpname.toString())
      val defName = TermName(tpname.toString())
     
       c.Expr[Any] {
         q""" 
              implicit final class CustomTags3(x: dom.Runtime.TagsAndTags2.type){
                  def $tagName() = new $tpname()
                }
              
              implicit val tags = new CustomTags3(dom.Runtime.TagsAndTags2)
              $tree""" 
       }  
      
      
//      val newStats = stats :+ 
//              q""" implicit final class CustomTags3(x: dom.Runtime.TagsAndTags2.type){
//                  def $tagName() = new $tpname()
//                }""" :+ 
//              q""" val tags = new CustomTags3(dom.Runtime.TagsAndTags2)"""
//                  
//      //println("BODY", newStats)
//                
//      
//      c.Expr[Any] {
//         q""" case object $objectName{}
//              $mods class $tpname[..$tparams] $ctorMods(...$paramss) 
//              extends { ..$earlydefns } with ..$parents { $self => ..$newStats }""" 
//       }   
    
    }
  }
  
  
  
  
  
  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class register extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro register.impl
  }
  
  object register {
    def impl(c: theC)(annottees: c.Expr[Any]*) = {
    import c.universe._
    val inputs: List[Tree] = annottees.map(_.tree).toList
    
    val tree = inputs(0)   
    
    val q"$mods val $pat = $expr" = tree
    
    val className = expr match { case Literal(Constant(s: String)) => TypeName(s) }
     
    val component = TypeName(s"${pat.toString()}Builder")
    val name = TypeName(s"${pat.toString()}Custom")
     
       c.Expr[Any] {
         q""" 
              implicit final class $className(x: dom.Runtime.TagsAndTags2.type){
                  def $pat() = new $component()
                }
                          
              $tree""" 
       }  
    }
  }
  
  
  
  
  
  
//  @compileTimeOnly("enable macro paradise to expand macro annotations")
//  class custom extends StaticAnnotation {
//    def macroTransform(annottees: Any*): Any = macro custom_impl
//  }
//  
//  def custom_impl(c: theC)(annottees: c.Expr[Any]*) = {
//    import c.universe._
//    val inputs:List[Tree] = annottees.map(_.tree).toList
//     val tree= inputs(0)
//     //val q"val $list:List[$t]= $files" = tree
//     
////     val q""" $mods class $tpname[..$tparams] $ctorMods(...$paramss) extends {..$earlydefns} with ..$parents { $self => $stats}
////          """ = tree
//     
////      print(show(q"""implicit val $t = $files(0)"""))
////     c.Expr[Any] {
////     q"""
////            implicit val fl1:$t = $files(0)
////      """
//     showCode(tree)
//     c.Expr(tree)
//     
//     c.Expr[Any] {
//       q""" implicit final class CustomTags3(x: dom.Runtime.TagsAndTags2.type){
//                def MyComponent() = new MyComponentBuilder()
//              }
//            
//            implicit val tags = new CustomTags3(dom.Runtime.TagsAndTags2)
//            $tree""" 
//     }
//   
//  }
  
}