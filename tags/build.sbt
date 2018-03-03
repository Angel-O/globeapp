import Dependencies._

name := "tags"

autoCompilerPlugins := true

enablePlugins(ScalaJSPlugin)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

libraryDependencies ++= {    
    Seq(
    scalaReflect.value,
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    scalaTest % Test,
    compilerPlugin(scalaMacros cross CrossVersion.full)
)}
