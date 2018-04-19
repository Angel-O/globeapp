import Dependencies._
import sbt._
import Keys._
import scala.sys.process._ 


lazy val commonSettings = Seq(
    organization := "com.Angelo",
    scalaVersion := "2.12.3",
    version      := "0.1.0-SNAPSHOT",
    scalacOptions ++= Seq("-feature", "-deprecation"),
    ensimeScalaVersion in ThisBuild := "2.12.3"
)

//lazy val execScriptMaster = taskKey[Unit]("Execute script")
lazy val execUi = taskKey[Unit]("run ui")
lazy val stageAll = taskKey[Unit]("stage all server")
lazy val execScript0 = taskKey[Unit]("run server")
lazy val execScript1 = taskKey[Unit]("run server")
lazy val execScript2 = taskKey[Unit]("run server")
lazy val execScript3 = taskKey[Unit]("run server")
lazy val execScript4 = taskKey[Unit]("run server")
lazy val execScript5 = taskKey[Unit]("run server")
lazy val all = inputKey[Unit]("run server in stage mode")
execScript0 := { "authentication-server/target/universal/stage/bin/authentication-server -Dhttp.port=3000" !}
execScript1 := { "app-server/target/universal/stage/bin/app-server -Dhttp.port=3001" !} 
execScript2 := { "review-server/target/universal/stage/bin/review-server -Dhttp.port=3002" !}
execScript3 := { "poll-server/target/universal/stage/bin/poll-server -Dhttp.port=3003" !}
execScript4 := { "suggestion-server/target/universal/stage/bin/suggestion-server -Dhttp.port=3004" !}
execScript5 := { "profile-server/target/universal/stage/bin/suggestion-server -Dhttp.port=3005" !}


//lazy val all = taskKey[Unit]("compile and then scalastyle")

lazy val root = (project in file("."))
    .aggregate(authenticationServer, appServer, pollServer, reviewServer, suggestionServer)
    .dependsOn(authenticationServer, appServer, pollServer, reviewServer, suggestionServer)
    .settings(
        commonSettings,
        stageAll := { "sbt ;clean ;stage" ! },
        //execUi := { "sbt runui" !}, 
        all in Compile := {
            //stageAll.value
            execScript0.value
            execScript1.value
            execScript2.value
            execScript3.value
            //execScript4.value
            //execScript5.value
            //execUi.value
            //(run in Compile in server).evaluated
            //(fastOptJS in Compile in ui).value
        }
    )

lazy val authenticationServer = (project in file("authentication-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

lazy val appServer = (project in file("app-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

lazy val pollServer = (project in file("poll-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

lazy val reviewServer = (project in file("review-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

lazy val suggestionServer = (project in file("suggestion-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

lazy val profileServer = (project in file("profile-server"))
    .dependsOn(sharedJVM, securityServer, common)
    .disablePlugins(WorkbenchPlugin)

//TODO turn this into a library
lazy val securityServer = (project in file("security-server"))
    .dependsOn(sharedJVM)
    .disablePlugins(WorkbenchPlugin)

lazy val tags = (project in file("tags"))
    .settings(commonSettings)

lazy val common = (project in file("common"))
    .dependsOn(sharedJVM)
    .settings(commonSettings)

lazy val shared = (project in file("shared"))
    .settings(
        commonSettings,
        //libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1",
        libraryDependencies += "io.github.cquiroz" %% "scala-java-time" % "2.0.0-M13",
        libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.9")

lazy val cross = (crossProject.crossType(CrossType.Full) in file("."))
    .settings(
        commonSettings,
        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
        libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13",
        libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.6.9",
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

lazy val sharedJVM = cross.jvm

lazy val sharedJS = cross.js

lazy val server = (project in file("server"))
    .dependsOn(sharedJVM, securityServer)
    .enablePlugins(PlayScala)
    .disablePlugins(WorkbenchPlugin)
    .settings(commonSettings)

lazy val execScript = taskKey[Unit]("Execute the shell script")

lazy val ui = (project in file("ui"))
    .dependsOn(tags, sharedJS)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
    .settings(
        commonSettings,
        mappings in (Compile, packageBin) ++= mappings.in(tags, Compile, packageBin).value,
        mappings in (Compile, packageSrc) ++= mappings.in(tags, Compile, packageSrc).value,
        execScript := { "sbt ~fastOptJS" ! },
        runus in Compile := {
            execScript.value
            (run in Compile in server).evaluated
        }
    )





lazy val launchserver = taskKey[Unit]("launch server project")
lazy val runus = inputKey[Unit]("run server and ui monitoring source files")

commands += Command.command("runui") { state =>
    //"project server" ::
    //"package" ::
    "project ui" ::
    "~fastOptJS" ::
    state
  }


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

commands += Command.command("runall") { state =>
    "project authenticationServer" :: "go" ::
    "project appServer" :: "go" ::
    "project ui" :: "~fastOptJS" ::
    state
}

commands += Command.command("createimages") { state =>
    "project authenticationServer" :: "docker:publishLocal" ::
    "project appServer" :: "docker:publishLocal" ::
    "project reviewServer" :: "docker:publishLocal" ::
    "project pollServer" :: "docker:publishLocal" ::
    "project profileServer" :: "docker:publishLocal" ::
    "project suggestionServer" :: "docker:publishLocal" ::
    state
}

//lazy val runall = inputKey[Unit]("run all servers")
//commands += Command.command("go1") { state =>
//    ";project appServer; clean ; stage; runit" :: 
//    state
//}
//commands += Command.command("go2") { state =>
//    ";project authenticationServer; clean ; stage; runit" :: 
//    state
//}


//import complete.DefaultParsers._
//import complete.Parser


//val separator: Parser[String] = "~"

//import scala.sys.process._ 
//
//lazy val execScript = taskKey[Unit]("Execute the shell script")
//
//lazy val ui = (project in file("ui"))
//    .dependsOn(tags, sharedJS)
//    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
//    .settings(
//        commonSettings,
//        // include the macro classes and resources in the main jar
//        mappings in (Compile, packageBin) ++= mappings.in(tags, Compile, packageBin).value,
//        // include the macro sources in the main source jar
//        mappings in (Compile, packageSrc) ++= mappings.in(tags, Compile, packageSrc).value,
//        execScript := {
//          "sbt ~fastOptJS" !
//        },
//        runus in Compile := {
////            (run in Compile in server).toTask("").value
//            //val sep = separator.parsed
//            //(fastOptJS in Compile)
//            execScript.value
//            //startWorkbenchServer.value
//            (run in Compile in server).evaluated
//            //Def.sequential(
//                //(fastOptJS in Compile)).value
//            
//            //Def.sequential((fastOptJS in Compile), (run in Compile in server).toTask("")).value
//        })
//        fastOptJS in Compile := (Def.taskDyn {
//            val c = (fastOptJS in Compile).value
//            Def.task {
//                val x = (run in Compile in server).toTask("").value
//                c
//            }
//        }).value,
        
        //mappings in (Compile, packageBin) ++= mappings.in(modelsJVM, Compile, packageBin).value,
        //mappings in (Compile, packageSrc) ++= mappings.in(modelsJVM, Compile, packageSrc).value,
        //commonSettings
//        name := "ui",
//        //run := (run in Compile).dependsOn((run in Compile in server).toTask("")).evaluated,
//        autoCompilerPlugins := true,
//        //EclipseKeys.preTasks := Seq(compile in Compile),
//        scalaJSUseMainModuleInitializer := true,
//        workbenchDefaultRootObject := Some(("./index-dev.html", "./ui")),
//        workbenchStartMode := WorkbenchStartModes.OnCompile,
//        libraryDependencies += scalaTest % Test,
//        libraryDependencies += "io.suzaku" %%% "diode" % "1.1.3",
//        libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "latest.release",
//        libraryDependencies += "com.thoughtworks.binding" %%% "futurebinding" % "latest.release",
//        libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
//        libraryDependencies += "com.github.dwickern" %% "scala-nameof" % "1.0.3" % "provided",
//        //libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0",
//        libraryDependencies += "fr.hmil" %%% "roshttp" % "2.1.0",
//        //libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.1",
//        //https:/stackoverflow.com/questions/43717198/symbol-type-none-scalacheck-shrink-is-missing-from-the-classpath
//        libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.+"//,
//        libraryDependencies ++= {
//            val silencerVersion = "0.6"
//            Seq(
//            compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
//            "com.github.ghik" %% "silencer-lib" % silencerVersion)
//        }
        
        //        inThisBuild(List(
//            scalaJSUseMainModuleInitializer := true,
//            workbenchDefaultRootObject := Some(("./index-dev.html", "./")),
//            workbenchStartMode := WorkbenchStartModes.OnCompile    
//        )),
//        unmanagedSourceDirectories in Compile ++= { unmanagedSourceDirectories in (models, Compile) },
//    )

//import play.twirl.sbt.Import.TwirlKeys._
//sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")


//.dependsOn(ProjectRef(uri("models/JVM"), "jvm"))


//,
        //name := "server",
        //run := (run in Compile).dependsOn(fastOptJS in Compile in ui).evaluated,
        //libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.1",
        //libraryDependencies += guice,
//        libraryDependencies += {
//            val reactiveMongoVer = "0.13.0-play26"
//            "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
//        },
        //libraryDependencies += "com.pauldijou" %% "jwt-play" % "0.14.1",
        //libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test//,
        //sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")//,
        //EclipseKeys.preTasks := Seq(compile in Compile)
        //,
        //mappings in (Compile, packageBin) ++= mappings.in(modelsJVM, Compile, packageBin).value,
        //mappings in (Compile, packageSrc) ++= mappings.in(modelsJVM, Compile, packageSrc).value
//    )
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