organization := "uk.gov.gds"

name := "gds-scala-utils"

version := "0.3.0-SNAPSHOT"

libraryDependencies ++= Seq(
        "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
        "gov.gds" %% "integration-tools" % "1.4-SNAPSHOT" % "test",
        "gov.gds" %% "management" % "5.7-SNAPSHOT",
        "org.scalatra" %% "scalatra" % "2.0.3",
        "org.scalatra" %% "scalatra-scalate" % "2.0.3",
        "org.scalatra" %% "scalatra-specs2" % "2.0.3" % "test",
        "javax.servlet" % "servlet-api" % "2.5" % "provided",
        "org.scalatest" %% "scalatest" % "1.7.2" % "test",
        "org.apache.httpcomponents" % "httpclient" % "4.1.2",
        "com.codahale" %% "jerkson" % "0.4.2",
        "joda-time" % "joda-time" % "2.1",
        "org.joda" % "joda-convert" % "1.2",
        "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT",
        "net.liftweb" %% "lift-json-ext" % "2.4",
        "org.scalatra" %% "scalatra-auth" % "2.0.4",
        "com.icegreen" % "greenmail" % "1.3",
        "com.codahale" %% "jerkson" % "0.4.2"
    )

parallelExecution in Test := false

resolvers ++= Seq(
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "repo.codahale" at "http://repo.codahale.com",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

publishArtifact in (Test, packageBin) := true

publishArtifact in (Test, packageSrc) := true
