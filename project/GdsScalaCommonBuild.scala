import sbt._
import org.sbtidea.SbtIdeaPlugin

object GdsScalaCommonBuild extends Build {

  def ideaSettings = {
    SbtIdeaPlugin.ideaSettings ++
      Seq(
        SbtIdeaPlugin.commandName := "idea"
      )
  }

  val root = Project("gds-common", file("."))
	 .aggregate(scalaUtils, mongoScalaUtils, govUkClients, govUkGuiceUtils, akkaQuartz)
  
  lazy val scalaUtils = Project("scala-utils", file("scala-utils"))
	  .settings(ideaSettings: _*)

  lazy val mongoScalaUtils = Project("mongo-utils", file("mongo-utils"))
    .settings(ideaSettings: _*)
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")

  lazy val govUkClients = Project("govuk-clients", file("govuk-clients"))
    .settings(ideaSettings: _*)
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")

  lazy val govUkGuiceUtils = Project("govuk-guice-utils", file("govuk-guice-utils"))
    .settings(ideaSettings: _*)

  lazy val akkaQuartz = Project("akka-quartz", file("akka-quartz"))
	  .settings(ideaSettings: _*)
}
