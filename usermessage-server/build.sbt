import scala.sys.process._ 

name := "usermessage-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice

lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val runit = inputKey[Unit]("run server in stage mode")
execScript := { s"${name.value}/target/universal/stage/bin/${name.value} -Dhttp.port=3001" !}
runit in Compile := { execScript.value }
        

commands += Command.command("go") { state =>
    "; clean ; stage; runit" :: 
    state
}

lazy val dcuScript = taskKey[Unit]("docker compose up")
lazy val dcu = inputKey[Unit]("run docker container")
dcuScript := { "docker-compose up"! }
dcu in Compile := { dcuScript.value }
commands += Command.command("dcu") { state => "; dcu" :: state }


//name := """app-server"""
//organization := "com.Angelo"
//
//version := "1.0-SNAPSHOT"
//
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
//
//scalaVersion := "2.12.3"
//
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.Angelo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.Angelo.binders._"

