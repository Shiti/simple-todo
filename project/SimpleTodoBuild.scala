import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object SimpleTodoBuild extends Build {

  val appName = "simple-todo"
  val appVersion = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "mysql" % "mysql-connector-java" % "5.1.32",
    anorm,
    jdbc
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies
  )
}
