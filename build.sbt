name := """Battle-Pets-Arena"""

version := "0.0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

// TODO: minimize library dependencies
libraryDependencies += jdbc

libraryDependencies += cache

libraryDependencies += ws

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.17"

libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.7"

libraryDependencies += filters

// TODO: comment
routesGenerator := InjectedRoutesGenerator