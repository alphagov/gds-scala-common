organization := "uk.gov.gds"

name := "gds-govuk-clients"

version := "0.1.1-SNAPSHOT"

scalacOptions += "-deprecation"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
        "javax.servlet" % "servlet-api" % "2.5" % "provided",
        "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test",
        "joda-time" % "joda-time" % "2.1",
        "org.joda" % "joda-convert" % "1.2",
        "org.apache.httpcomponents" % "httpclient" % "4.1.2",
        "jmimemagic" % "jmimemagic" % "0.1.1",
        "com.google.guava" % "guava" % "13.0",
	    "com.google.code.findbugs" % "jsr305" % "1.3.+"
    )

parallelExecution in Test := false

resolvers ++= Seq(
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "repo.codahale" at "http://repo.codahale.com",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "repo scalatools releases" at "https://oss.sonatype.org/content/groups/scala-tools/"
)

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
