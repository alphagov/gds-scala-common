import sbt._
import org.sbtidea.SbtIdeaPlugin
import PlayProject._

object GdsScalaCommonBuild extends Build {
 
	// As a result of play 2.0.2 importing the SBT idea plugin and renaming it "idea" we need
  	// to configure a version of this plugin against all non-play projects so that we can run the
  	// "idea" command against them

  def ideaSettings = {
    SbtIdeaPlugin.ideaSettings ++
      Seq(
        SbtIdeaPlugin.commandName := "idea"
      )
  }

  val root = PlayProject("gds-common", "2.0-SNAPSHOT", Seq(), file("."), mainLang = SCALA)
	.aggregate(scalaUtils, mongoScalaUtils, govUkClients)
  
  lazy val scalaUtils = Project("scala-utils", file("scala-utils"))
	  .settings(ideaSettings: _*)

  lazy val mongoScalaUtils = Project("mongo-utils", file("mongo-utils"))
    .settings(ideaSettings: _*)
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")

  lazy val govUkClients = Project("govuk-clients", file("govuk-clients"))
    .settings(ideaSettings: _*)
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")
    //.dependsOn(mongoScalaUtils % "test->test;test->compile;compile->compile")
}
