import sbt._
import Keys._
import Dependencies._

object CommonServer{
    
//    val settings = commonSettings ++ Seq(
//        sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl"))
    val dependencies = {
        
        Seq(
        jwtPlay,
        nameOf,
        scalaTestPlus % Test)
    }
    
    val reactiveMongo = Dependencies.reactiveMongo
}

