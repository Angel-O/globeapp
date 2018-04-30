import scala.sys.process._ 

name := "messaging-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice

lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val runit = inputKey[Unit]("run server in stage mode")
execScript := { s"${name.value}/target/universal/stage/bin/${name.value} -Dhttp.port=3006" !}
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