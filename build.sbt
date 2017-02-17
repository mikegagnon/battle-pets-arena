name := """Battle-Pets-Arena"""

version := "0.0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

// TODO: minimize library dependencies

libraryDependencies += jdbc

libraryDependencies += cache

libraryDependencies += ws

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test


// TODO: comment
routesGenerator := InjectedRoutesGenerator