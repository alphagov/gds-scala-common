organization := "uk.gov.gds"

name := "akka-quartz"

version := "1.2-SNAPSHOT"

scalacOptions += "-deprecation"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
        "org.quartz-scheduler" % "quartz" % "2.2.1" % "compile",
        "com.typesafe.akka" %% "akka-actor" % "2.4.1" % "compile",
        "com.typesafe.akka" %% "akka-testkit" % "2.4.1" % "test",
        "org.specs2" %% "specs2-core" % "3.0" % "test",
        "org.slf4j" % "slf4j-simple" % "1.7.18"
    )

parallelExecution in Test := false

resolvers ++= Seq(
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases",
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
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
