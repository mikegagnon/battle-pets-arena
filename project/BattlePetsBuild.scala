// Based off the book "Mastering Play Framework for Scala"

import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName = "BattlePetsArena"
  val appVersion = "0.0.1"


  val appDependencies = Seq()

  val main = Project(appName,
    file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies
    )
}