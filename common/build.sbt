import Dependencies._

name := "common"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += nameOf
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test