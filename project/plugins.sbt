resolvers ++= Seq(
    "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
    "GDS maven repo snapshots" at "http://alphagov.github.com/maven/snapshots",
    "GDS maven repo releases" at "http://alphagov.github.com/maven/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
