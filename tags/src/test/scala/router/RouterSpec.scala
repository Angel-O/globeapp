package router

import org.scalatest._

class RouterSpec extends FlatSpec with Matchers {
  "Path fragments" should "be mapped to correct regex patterns" in {
    import DynamicRoute._
    val path = "users" / ":username" / "posts" / Int
    
    path.toString shouldEqual s"(users)${DynamicRoute.alphaNumeric}(posts)${DynamicRoute.numeric}"
  }
  
  "Dynamic routes" should "extract correct parameters" in {
    import DynamicRoute._
    val path = "users" / ":username" / "posts" / Int
    
    val route = new DynamicRoute("api", path)
      
    //info(path.r.toString)
    //info(path.normalizeUrl("users/paul/posts/3"))
    //info(path.normalizeFragmentSeq.toString)
    
    //println("HELLO", route.leftSide) //where is this coming from ???
       
    route.params("users/paul/posts/3") shouldEqual List("paul", "3")
  }
}
