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

  val root = PlayProject("gds-common", "1.0-SNAPSHOT", Seq(), file("."), mainLang = SCALA)
	.aggregate(commonScalaUtils)
  
  lazy val commonScalaUtils = Project("scala-utils", file("scala-utils"))
	 .settings(ideaSettings: _*)
}
