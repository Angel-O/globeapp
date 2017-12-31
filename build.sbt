import Dependencies._

lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT" 
)

lazy val root = (project in file(".")).aggregate(ui, server)//.dependsOn(ui, server)

lazy val ui = (project in file("ui"))
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .settings(
        inThisBuild(List(
            scalaJSUseMainModuleInitializer := true,
            workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
            workbenchStartMode := WorkbenchStartModes.OnCompile    
        )),
        commonSettings,
        name := "ui",
        libraryDependencies += scalaTest % Test,
        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
        libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release"
    )

lazy val server = (project in file("server"))
    .settings(
        commonSettings,
        name := "server",
        libraryDependencies += scalaTest % Test
      )