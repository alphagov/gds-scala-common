resolvers ++= Seq(
    "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0-M2-TYPESAFE")

addSbtPlugin("play" % "sbt-plugin" % "2.0.3")

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

//addSbtPlugin("gov.gds" %% "sbt-version-info-plugin" % "1.2.3")
