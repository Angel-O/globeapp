name := "models"

//lazy val models = (project in file(".")).aggregate(crossModelsJS, crossModelsJVM)
//
//lazy val crossModels = (crossProject.crossType(CrossType.Pure) in file(".")).
//  settings(
//    name := "models",
//    version := "0.1-SNAPSHOT"
//  ).
//  jvmSettings(
//    libraryDependencies += "io.github.cquiroz" %% "scala-java-time" % "2.0.0-M13",
//    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.9"
//  ).
//  jsSettings(
//    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M13",
//    libraryDependencies += "com.typesafe.play" %%% "play-json" % "2.6.9"
//  )
//
//lazy val crossModelsJVM = crossModels.jvm
//lazy val crossModelsJS = crossModels.js

lazy val models = (project in file("."))
.settings(
    libraryDependencies += "io.github.cquiroz" %% "scala-java-time" % "2.0.0-M13",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.9"
)

//.enablePlugins(PlayScala)
//libraryDependencies += guice
//libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test