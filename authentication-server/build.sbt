name := "authentication-server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice
