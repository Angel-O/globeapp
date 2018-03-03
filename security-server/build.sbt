name := "security-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += guice
