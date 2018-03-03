name := "server"

enablePlugins(PlayScala)

libraryDependencies ++= CommonServer.dependencies
libraryDependencies += CommonServer.reactiveMongo
libraryDependencies += guice

import play.twirl.sbt.Import.TwirlKeys._
sourceDirectories in (Compile, compileTemplates) += file("server/target/scala-2.12/twirl")


