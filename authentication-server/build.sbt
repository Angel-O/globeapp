import scala.sys.process._ 

name := "authentication-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice

lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val runit = inputKey[Unit]("run server in stage mode")
execScript := { s"${name.value}/target/universal/stage/bin/${name.value} -Dhttp.port=3000" !}
runit in Compile := { execScript.value }
        

commands += Command.command("go") { state =>
    "; clean ; stage; runit" :: 
    state
}

