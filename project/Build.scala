import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "simple-todo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "mysql" % "mysql-connector-java" % "5.1.18"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
