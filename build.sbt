import Dependencies._
//import sbt.Keys._

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT" 
)

//lazy val commonDependencies = Seq(
//    libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1"
//)

lazy val root = (project in file(".")).aggregate(ui, server)//.dependsOn(ui, server)

lazy val ui = (project in file("ui"))
    .dependsOn(tags, apimodelsJS)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .settings(
//        inThisBuild(List(
//            scalaJSUseMainModuleInitializer := true,
//            workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
//            workbenchStartMode := WorkbenchStartModes.OnCompile    
//        )),
//        unmanagedSourceDirectories in Compile ++= { unmanagedSourceDirectories in (models, Compile) },
        // include the macro classes and resources in the main jar
        mappings in (Compile, packageBin) ++= mappings.in(tags, Compile, packageBin).value,
        // include the macro sources in the main source jar
        mappings in (Compile, packageSrc) ++= mappings.in(tags, Compile, packageSrc).value,
        //mappings in (Compile, packageBin) ++= mappings.in(modelsJVM, Compile, packageBin).value,
        //mappings in (Compile, packageSrc) ++= mappings.in(modelsJVM, Compile, packageSrc).value,
        commonSettings,
        name := "ui",
        autoCompilerPlugins := true,
        EclipseKeys.preTasks := Seq(compile in Compile),
        scalaJSUseMainModuleInitializer := true,
        workbenchDefaultRootObject := Some(("./index-dev.html", "./ui")),
        workbenchStartMode := WorkbenchStartModes.OnCompile,
        libraryDependencies += scalaTest % Test,
        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
        libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release",
        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
        //https:/stackoverflow.com/questions/43717198/symbol-type-none-scalacheck-shrink-is-missing-from-the-classpath
        libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.+"

    )

//import play.twirl.sbt.Import.TwirlKeys._
//sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")

lazy val server = (project in file("server"))
    //.dependsOn(ProjectRef(uri("models/JVM"), "jvm"))
    .dependsOn(apimodelsJVM)
    .enablePlugins(PlayScala)
    .settings(
        commonSettings,
        name := "server",
        libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1",
        libraryDependencies += guice,
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
        EclipseKeys.preTasks := Seq(compile in Compile)
        //,
        //mappings in (Compile, packageBin) ++= mappings.in(modelsJVM, Compile, packageBin).value,
        //mappings in (Compile, packageSrc) ++= mappings.in(modelsJVM, Compile, packageSrc).value
        )
//        libraryDependencies += scalaTest % Test,
//        libraryDependencies ++= { 
//            val akkaVersion = "2.5.8"
//            Seq("com.typesafe.akka" %% "akka-actor"      % akkaVersion, 
//                "com.typesafe.akka" %% "akka-http-core"  % "10.0.11",
//                "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11", 
//                "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
//                "ch.qos.logback"    %  "logback-classic" % "1.2.3",
//                "com.typesafe.akka" %% "akka-testkit"    % akkaVersion % Test)
//        }
//      )

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

//lazy val models = (project in file("models"))
//    .aggregate(modelsJVM, modelsJS)
//    .settings(commonSettings)

lazy val shared = (project in file("shared"))
    .settings(
        commonSettings,
        name := "shared"    
    )
    .enablePlugins(ScalaJSPlugin)

lazy val apimodels = (crossProject.crossType(CrossType.Full) in file("."))
    .settings(
        commonSettings,
        name := "apimodels",
        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1"
        EclipseKeys.useProjectId := true
    )
    .jsConfigure(_.enablePlugins(ScalaJSPlugin))
    .jvmConfigure(_.enablePlugins(ScalaJSPlugin))
    .jvmSettings(
        // Add JVM-specific settings here
        //libraryDependencies += "org.scala-js" %% "scalajs-dom" % "0.9.3"
        unmanagedSourceDirectories in Compile += baseDirectory.value / "jvm",
        libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1"
    )
    .jsSettings(
        // Add JS-specific settings here
        //libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.3"
        //sourceMapsBase := baseDirectory.value / "..",
        unmanagedSourceDirectories in Compile += baseDirectory.value / "js",
        libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1"
    )

lazy val apimodelsJVM = apimodels.jvm

lazy val apimodelsJS = apimodels.js


