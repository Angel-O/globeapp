import Dependencies._

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT" 
)

lazy val root = (project in file(".")).aggregate(ui, server)//.dependsOn(ui, server)

lazy val ui = (project in file("ui"))
    .dependsOn(tags)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .settings(
//        inThisBuild(List(
//            scalaJSUseMainModuleInitializer := true,
//            workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
//            workbenchStartMode := WorkbenchStartModes.OnCompile    
//        )),
        // include the macro classes and resources in the main jar
        mappings in (Compile, packageBin) ++= mappings.in(tags, Compile, packageBin).value,
        // include the macro sources in the main source jar
        mappings in (Compile, packageSrc) ++= mappings.in(tags, Compile, packageSrc).value,
        commonSettings,
        name := "ui",
        autoCompilerPlugins := true,
        scalaJSUseMainModuleInitializer := true,
        workbenchDefaultRootObject := Some(("./index-dev.html", "./ui")),
        workbenchStartMode := WorkbenchStartModes.OnCompile,
        libraryDependencies += scalaTest % Test,
        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
        libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release",
        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.+"

    )

lazy val server = (project in file("server"))
    .settings(
        commonSettings,
        name := "server",
        libraryDependencies += scalaTest % Test
      )

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

lazy val tags = (project in file("tags"))
    .enablePlugins(ScalaJSPlugin)
    .settings(
        commonSettings,
        name := "tags",
        autoCompilerPlugins := true,
        libraryDependencies += scalaReflect.value,
        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )