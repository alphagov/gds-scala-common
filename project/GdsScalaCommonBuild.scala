import sbt._
import org.sbtidea.SbtIdeaPlugin
import PlayProject._

object GdsScalaCommonBuild extends Build {

  def ideaSettings = {
    SbtIdeaPlugin.ideaSettings ++
      Seq(
        SbtIdeaPlugin.commandName := "idea"
      )
  }

  val root = PlayProject("gds-common", "2.0-SNAPSHOT", Seq(), file("."), mainLang = SCALA)
	 .aggregate(scalaUtils, mongoScalaUtils, govUkClients, govUkGuiceUtils)
  
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
}
