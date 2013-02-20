organization := "uk.gov.gds"

name := "govuk-guice-utils"

version := "0.0.1-SNAPSHOT"

scalacOptions += "-deprecation"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
    "com.google.inject" % "guice" % "3.0"
)

parallelExecution in Test := false

publishArtifact in (Test, packageSrc) := true

publishTo in ThisBuild <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "alphagov github " + publishType,
            file(System.getProperty("user.home") + "/alphagov.github.com/maven/" + publishType)
        )
    )
}
