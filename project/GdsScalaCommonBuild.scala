import sbt._

object GdsScalaCommonBuild extends Build {
 
  val root = Project("gds-common", file(".")).aggregate(commonScalaUtils)
  
  lazy val commonScalaUtils = Project("scala-utils", file("scala-utils"))
}
