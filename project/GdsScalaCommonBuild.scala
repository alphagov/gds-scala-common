import sbt._

object GdsScalaCommonBuild extends Build {

    val root = Project("gds-common", file("."))
	 .aggregate(scalaUtils, mongoScalaUtils, govUkClients, govUkGuiceUtils)
  
  lazy val scalaUtils = Project("scala-utils", file("scala-utils"))

  lazy val mongoScalaUtils = Project("mongo-utils", file("mongo-utils"))
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")

  lazy val govUkClients = Project("govuk-clients", file("govuk-clients"))
    .dependsOn(scalaUtils % "test->test;test->compile;compile->compile")

  lazy val govUkGuiceUtils = Project("govuk-guice-utils", file("govuk-guice-utils"))
}
