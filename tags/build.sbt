//import Dependencies._
//
//scalaVersion := "2.12.3"
//
//enablePlugins(ScalaJSPlugin)
//
//enablePlugins(WorkbenchPlugin)
//
//scalaJSUseMainModuleInitializer := true
//
//workbenchStartMode := WorkbenchStartModes.OnCompile
//
//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
//
//lazy val ui = (project in file(".")).
//  settings(
//    inThisBuild(List(
//      organization := "com.Angelo",
//      scalaVersion := "2.12.3",
//      version      := "0.1.0-SNAPSHOT",
//      workbenchDefaultRootObject := Some(("./index-dev.html", "./"))
//    )),
//    name := "ui",
//    libraryDependencies += scalaTest % Test,
//    libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
//    libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release"
//  )