import Dependencies._

lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT" 
)

enablePlugins(ScalaJSPlugin)

enablePlugins(WorkbenchPlugin)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val root = (project in file("ui")).
  settings(
    inThisBuild(List(
        scalaJSUseMainModuleInitializer := true,
        workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
        workbenchStartMode := WorkbenchStartModes.OnCompile    
    )),
    commonSettings,
    name := "ScalawebUI",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
    libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release"
  )

lazy val root = (project in file("server")).
  settings(
    commonSettings,
    name := "ScalawebServer",
    libraryDependencies += scalaTest % Test
  )