import java.util.jar._

scalaVersion := "2.10.0"

publishArtifact := false

packageOptions in ThisBuild <+= (version, name) map { (v, n) =>
  Package.ManifestAttributes(
    Attributes.Name.IMPLEMENTATION_VERSION -> v,
    Attributes.Name.IMPLEMENTATION_TITLE -> n,
    Attributes.Name.IMPLEMENTATION_VENDOR -> "Government Digital Service"
  )
}

resolvers ++= Seq(
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)
