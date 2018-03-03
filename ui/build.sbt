import Dependencies._

name := "ui"

autoCompilerPlugins := true

scalaJSUseMainModuleInitializer := true

workbenchDefaultRootObject := Some(("./index-dev.html", "./ui"))

workbenchStartMode := WorkbenchStartModes.OnCompile

libraryDependencies += scalaTest % Test

libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release"

libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release"

libraryDependencies += "io.suzaku" %%% "diode" % "1.1.3"

libraryDependencies += compilerPlugin(scalaMacros cross CrossVersion.full)

libraryDependencies += nameOf

//libraryDependencies += "fr.hmil" %%% "roshttp" % "2.1.0"

//https:/stackoverflow.com/questions/43717198/symbol-type-none-scalacheck-shrink-is-missing-from-the-classpath
libraryDependencies += scalaCheck//,

//run := (run in Compile).dependsOn((run in Compile in server).toTask("")).evaluated,