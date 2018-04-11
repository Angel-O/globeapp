import scala.sys.process._ 

name := "suggestion-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice
libraryDependencies += ws // can be removed at some point

lazy val execScript = taskKey[Unit]("Execute the shell script")
lazy val runit = inputKey[Unit]("run server in stage mode")
execScript := { s"${name.value}/target/universal/stage/bin/${name.value} -Dhttp.port=3004" !}
runit in Compile := { execScript.value }
        

commands += Command.command("go") { state =>
    "; clean ; stage; runit" :: 
    state
}

