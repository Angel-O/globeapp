package router

//val routes = List(
//        HomePageURI -> home, 
//        RegisterPageURI -> register,
//        UserEditPageURI -> userEdit,
//        s"$UserEditPageURI/:username" -> userEdit, //TODO !!!
//        SamplePageURI -> sample)
//        
//     routes
import scala.language.implicitConversions

object DynamicRoute{
  implicit class Path(x: String) {
    def toPath(): FragmentSeq = FragmentSeq(Seq(StringFragment(x)))
  }
  //implicit def toPath(x: String): FragmentSeq = FragmentSeq(Seq(StringFragment(x)))
  implicit def toFragment(x: String): Fragment = StringFragment(x)
  implicit def toFragment(x: Any): Fragment = TypeFragment(x)
  //implicit def toFragmentSeq(x: String): FragmentSeq = FragmentSeq(Seq(StringFragment(x)))
  implicit def toFragmentSeq(x: Fragment): FragmentSeq = FragmentSeq(Seq(x))
  implicit class ToFrag(x: String) {
    def toFragment = StringFragment(x)
  }
  val alpha = "([a-zA-Z]+)"
  val numeric = "([0-9]+)"
}
  
//class DynamicRoute(baseUri: String, path: FragmentSeq) {
//  
//  def params(url: String) = path.getRouteParams(url: String)
//  
//  def paramsEasy(url: String) = path.getParamsEasy(url: String)
//}

sealed trait Fragment{
  def /(fragment: Fragment) = {
    FragmentSeq(Seq(this, fragment))
  }
  def /(fragments: FragmentSeq) = {
    FragmentSeq(this +: fragments.fragments)
  }
  val isParam: Boolean
}
case class StringFragment(x: String) extends Fragment{
  val isParam = x.startsWith(":")
  override def toString = x
}
case class TypeFragment(x: Any) extends Fragment{
  val isParam = true
  override def toString = x.toString
}


case object FragmentSeq{
  def test(url: String) = {
    import DynamicRoute._
    val toFragments = url.split("/").map(_.toFragment).toSeq
    val seq = new FragmentSeq(toFragments).toLiteral.tail.init
    seq
  }
}
case class FragmentSeq(fragments: Seq[Fragment]){
  val maxLength = 21
  def addFragment(fragment: Fragment) = FragmentSeq(fragments :+ fragment)
  def /(fragment: Fragment) = addFragment(fragment)
  def length = fragments.length 
  def r = toString.r 
  override def toString = {
    fragments.map(x => x match {
      case sf @ StringFragment(e) => if (sf.isParam) DynamicRoute.alpha else s"($e)"
      case TypeFragment(_) => DynamicRoute.numeric
      //case _ => s"(${x.toString})"
    }).mkString("")
  } 
  
  def toLiteral = {
    fragments.map(x => s"(${x.toString})").mkString("")
  }
  
  def getParams(url: String) = {
    import DynamicRoute._
    val normailzedUrl = normalizeUrl(url)
    val normalizedFragmentSeq = normalizeFragmentSeq //"(users)(Str Patt)(posts)(Int Patt)(placeholder)(placeholder)..."
    val pattern = normalizedFragmentSeq.r 
    val pattern(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u) = normailzedUrl
    (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u).productIterator.toSeq
  } 
  
  def matchesUrl(url: String) = {
    val length = url.tail.split('/').size
    val pathLiteral = url.tail.split('/').mkString("")
    (pathLiteral, length)  match {
//      case (this.r(_), 1) | (this.r(_, _),2) | (this.r(_, _, _), 3) | (this.r(_, _, _, _),4) | 
//           (this.r(_, _, _ ,_ , _),5) | (this.r(_, _, _, _, _, _), 6) | (this.r(_, _, _, _, _, _, _), 7) | 
//           (this.r(_, _, _, _, _, _, _, _), 8) | (this.r(_, _, _, _, _, _, _, _, _), 9) | 
//           (this.r(_, _, _, _, _, _, _, _, _, _), 10) => true // contine until max length....or...(see below)
      
      // TODO pattern matching against regEx is not necessary (length comparison should be enough)
      case (this.r(_*), x) => x <= maxLength && this.length == length 
      case _ => false
    }
  }
  
  def normalizeUrl(url: String) = {
    var split = url.split('/')
    val missingFragments = maxLength - split.length
    for(i <- 0 until missingFragments) {
      split = split :+ "placeholder"
    }
    split.mkString("") // ===> userspaulposts3placeholderplaceholder.... 
  }
  
  //Logic to go in Router
  def getParamsEasy(url: String) = {
    import DynamicRoute._
    val toFragments = url.split('/').map(_.toFragment).toSeq
    val params = toFragments.map(x => x match {
      case p @ StringFragment(s) => if (p.isParam) s.tail else (if (s forall Character.isDigit) s else "")
    })
    params.filterNot(_.isEmpty).toList
  }
  def paramsIndices: Seq[Int] = {
    fragments.collect{x => x.isParam match {case true => fragments.indexOf(x)}}.toSeq
  } 
  def getRouteParams(url: String) = {
    val params = getParams(url)
    params.filter(x => paramsIndices.contains(params.indexOf(x)))
  } 
  def normalizeFragmentSeq: FragmentSeq = {
    val missingFragments = maxLength - this.length
    var normalized = this
    for(i <- 0 until missingFragments) {
      normalized = normalized / StringFragment("placeholder")
    }
    normalized
    
    //1. "users" / ":username" / "posts" / Int     => toString
    //2. "(users)/(Str Patt)/(posts)/(Int Patt)/(placeholder)/(placeholder)..."      => split("/") & mkString("")
    //3. "(users)(Str Patt)(posts)(Int Patt)(placeholder)(placeholder)..."
  }
}



