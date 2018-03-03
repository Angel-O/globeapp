import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val nameOf = "com.github.dwickern" %% "scala-nameof" % "1.0.3" % "provided"
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.+"
  lazy val scalaMacros = "org.scalamacros" % "paradise" % "2.1.0"
  lazy val scalaTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
  lazy val jwtPlay = "com.pauldijou" %% "jwt-play" % "0.14.1"
  lazy val reactiveMongo = {
      val reactiveMongoVer = "0.13.0-play26"
      "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
  }
}
