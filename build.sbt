import Dependencies._
//import sbt.Keys._

lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT",
    scalacOptions ++= Seq("-feature"),
    ensimeScalaVersion in ThisBuild := "2.12.3"
)

lazy val root = (project in file("."))
    .aggregate(ui, server)
    .settings(
        commonSettings
        //update / aggregate := false
        //run / aggregate := false,
        //fastOptJS / aggregate := false
    )//.dependsOn(ui, server)

lazy val cross = (crossProject.crossType(CrossType.Full) in file("."))
    .settings(
        commonSettings,
        //name := "shared",
        libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
        EclipseKeys.useProjectId := true
    )
    .jsConfigure(_.enablePlugins(ScalaJSPlugin))
    .jvmSettings(
        // Add JVM-specific settings here
        //libraryDependencies += "org.scala-js" %% "scalajs-dom" % "0.9.3"
        //unmanagedSourceDirectories in Compile += baseDirectory.value / "jvm",
        //libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1"
    )
    .jsSettings(
        // Add JS-specific settings here
        //libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.3"
        //sourceMapsBase := baseDirectory.value / "..",
        //unmanagedSourceDirectories in Compile += baseDirectory.value / "js",
        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1"
    )

//import complete.DefaultParsers._
//import complete.Parser

lazy val launchserver = taskKey[Unit]("launch server project")
lazy val runus = inputKey[Unit]("run server and ui moitoring source files")
//val separator: Parser[String] = "~"

import scala.sys.process._ 

lazy val execScript = taskKey[Unit]("Execute the shell script")


lazy val ui = (project in file("ui"))
    .dependsOn(tags, sharedJS)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .settings(
//        inThisBuild(List(
//            scalaJSUseMainModuleInitializer := true,
//            workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
//            workbenchStartMode := WorkbenchStartModes.OnCompile    
//        )),
//        unmanagedSourceDirectories in Compile ++= { unmanagedSourceDirectories in (models, Compile) },
        execScript := {
          "sbt ~fastOptJS" !
        },
        runus in Compile := {
//            (run in Compile in server).toTask("").value
            //val sep = separator.parsed
            //(fastOptJS in Compile)
            execScript.value
            //startWorkbenchServer.value
            (run in Compile in server).evaluated
            //Def.sequential(
                //(fastOptJS in Compile)).value
            
            //Def.sequential((fastOptJS in Compile), (run in Compile in server).toTask("")).value
        },
//        fastOptJS in Compile := (Def.taskDyn {
//            val c = (fastOptJS in Compile).value
//            Def.task {
//                val x = (run in Compile in server).toTask("").value
//                c
//            }
//        }).value,
        // include the macro classes and resources in the main jar
        mappings in (Compile, packageBin) ++= mappings.in(tags, Compile, packageBin).value,
        // include the macro sources in the main source jar
        mappings in (Compile, packageSrc) ++= mappings.in(tags, Compile, packageSrc).value,
        //mappings in (Compile, packageBin) ++= mappings.in(modelsJVM, Compile, packageBin).value,
        //mappings in (Compile, packageSrc) ++= mappings.in(modelsJVM, Compile, packageSrc).value,
        commonSettings,
        name := "ui",
        //run := (run in Compile).dependsOn((run in Compile in server).toTask("")).evaluated,
        autoCompilerPlugins := true,
        //EclipseKeys.preTasks := Seq(compile in Compile),
        scalaJSUseMainModuleInitializer := true,
        workbenchDefaultRootObject := Some(("./index-dev.html", "./ui")),
        workbenchStartMode := WorkbenchStartModes.OnCompile,
        libraryDependencies += scalaTest % Test,
        libraryDependencies += "io.suzaku" %%% "diode" % "1.1.3",
        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
        libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release",
        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        libraryDependencies += "com.github.dwickern" %% "scala-nameof" % "1.0.3" % "provided",
        //libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0",
        libraryDependencies += "fr.hmil" %%% "roshttp" % "2.1.0",
        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
        //https:/stackoverflow.com/questions/43717198/symbol-type-none-scalacheck-shrink-is-missing-from-the-classpath
        libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.+"//,
//        libraryDependencies ++= {
//            val silencerVersion = "0.6"
//            Seq(
//            compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
//            "com.github.ghik" %% "silencer-lib" % silencerVersion)
//        }
    )

import play.twirl.sbt.Import.TwirlKeys._
//sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")

lazy val server = (project in file("server"))
    //.dependsOn(ProjectRef(uri("models/JVM"), "jvm"))
    .dependsOn(sharedJVM, securityServer)
    .enablePlugins(PlayScala)
    .disablePlugins(WorkbenchPlugin)
    .settings(
        commonSettings,
        name := "server",
        //run := (run in Compile).dependsOn(fastOptJS in Compile in ui).evaluated,
        //libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1",
        libraryDependencies += guice,
        libraryDependencies += {
            val reactiveMongoVer = "0.13.0-play26"
            "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
        },
        libraryDependencies += "com.pauldijou" %% "jwt-play" % "0.14.1",
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
        sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")//,
        //EclipseKeys.preTasks := Seq(compile in Compile)
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

lazy val authenticationServer = (project in file("authentication-server"))
    .dependsOn(sharedJVM, securityServer)
    .enablePlugins(PlayScala)
    .disablePlugins(WorkbenchPlugin)
    .settings(
        commonSettings,
        name := "authentication-server",
        libraryDependencies += guice,
        libraryDependencies += {
            val reactiveMongoVer = "0.13.0-play26"
            "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
        },
        libraryDependencies += "com.pauldijou" %% "jwt-play" % "0.14.1",
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
        sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")
    )

lazy val appServer = (project in file("app-server"))
    .dependsOn(sharedJVM, securityServer)
    .enablePlugins(PlayScala)
    .disablePlugins(WorkbenchPlugin)
    .settings(
        commonSettings,
        name := "app-server",
        libraryDependencies += guice,
        libraryDependencies += {
            val reactiveMongoVer = "0.13.0-play26"
            "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
        },
        libraryDependencies += "com.pauldijou" %% "jwt-play" % "0.14.1",
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
        sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")
    )

lazy val securityServer = (project in file("security-server"))
    .dependsOn(sharedJVM)
    .enablePlugins(PlayScala)
    .disablePlugins(WorkbenchPlugin)
    .settings(
        commonSettings,
        name := "security-server",
        libraryDependencies += guice,
        libraryDependencies += "com.pauldijou" %% "jwt-play" % "0.14.1",
        libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
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
        libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )



//lazy val shared = (project in file("shared"))
//    .settings(
//        commonSettings,
//        name := "shared"    
//    )
//    .enablePlugins(ScalaJSPlugin)

lazy val shared = (project in file("shared"))
    //.aggregate(sharedJVM, sharedJS)
    .settings(commonSettings)

//lazy val shared = (crossProject.crossType(CrossType.Full) in file("shared"))
//    .settings(
//        commonSettings,
//        name := "shared",
//        libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
//        EclipseKeys.useProjectId := true
//    )
//    .jsConfigure(_.enablePlugins(ScalaJSPlugin))
//    .jvmSettings(
//        // Add JVM-specific settings here
//        //libraryDependencies += "org.scala-js" %% "scalajs-dom" % "0.9.3"
//        //unmanagedSourceDirectories in Compile += baseDirectory.value / "jvm",
//        //libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1"
//    )
//    .jsSettings(
//        // Add JS-specific settings here
//        //libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.3"
//        //sourceMapsBase := baseDirectory.value / "..",
//        //unmanagedSourceDirectories in Compile += baseDirectory.value / "js",
//        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1"
//    )

lazy val sharedJVM = cross.jvm

lazy val sharedJS = cross.js

commands += Command.command("lla") { state =>
    "project ui" ::
    "fastOptJS" ::
    "project server" ::
    "run" ::
    "project root" ::
    //"lla" ::
    state
  }

commands += Command.command("devserver") { state =>
    "project ui" ::
    "fastOptJS" ::
    "project server" ::
    "run" ::
    "project root" ::
    "lla" ::
    state
  }

commands += Command.command("devui") { state =>
    //"project server" ::
    //"package" ::
    //"project ui" ::
    "runserver" ::
    "~fastOptJS" ::
    state
  }

commands += Command.command("ser") { state =>
    "project server" ::
    "run" ::
    "project root" ::
    state
  }

commands += Command.command("clie") { state =>
    "project ui" ::
    "~fastOptJS" ::
    "project root" ::
    state
  }


///**
//  * Convert the given command string to a release step action, preserving and      invoking remaining commands
//  * Note: This was copied from https://github.com/sbt/sbt-release/blob/663cfd426361484228a21a1244b2e6b0f7656bdf/src/main/scala/ReleasePlugin.scala#L99-L115
//  */
//def runCommandAndRemaining(command: String): State => State = { st: State =>
//  import sbt.complete.Parser
//  @annotation.tailrec
//  def runCommand(command: String, state: State): State = {
//    val nextState = Parser.parse(command, state.combinedParser) match {
//      case Right(cmd) => cmd()
//      case Left(msg) => throw sys.error(s"Invalid programmatic input:\n$msg")
//    }
//    nextState.remainingCommands.toList match {
//      case Nil => nextState
//      case head :: tail => runCommand(head, nextState.copy(remainingCommands = tail))
//    }
//  }
//  runCommand(command, st.copy(remainingCommands = Nil)).copy(remainingCommands = st.remainingCommands)
//}


//onLoad in Global := (onLoad in Global)
//.value andThen {s: State => "project ui" :: "fastOptJS" :: s} //andThen {}

//onLoad in Global := (onLoad in Global)
//.value andThen {s: State => "project ui" :: "fastOptJS" :: s} //andThen {}

//andThen {state => "project ui" ::
//    "~fastOptJS" ::
//    state}