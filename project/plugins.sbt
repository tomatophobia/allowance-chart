addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.16")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// sbt-revolver is a plugin for SBT enabling a super-fast development turnaround for your Scala applications
// https://github.com/spray/sbt-revolver
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// for circe generic-extras
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
