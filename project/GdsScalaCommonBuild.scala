import sbt._

object RouterBuild extends Build {
 
  val root = Project("gds-common", file(".")).aggregate(commonScalaUtils)
  
  lazy val commonScalaUtils = Project("scala-utils", file("scala-utils"))
}
