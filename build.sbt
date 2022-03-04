import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.easywritten"
ThisBuild / organizationName := "easywritten"

lazy val root = (project in file("."))
  .settings(
    name := "allowance-chart",
    libraryDependencies ++= zio.all ++ cats.all ++ http4s.all ++ tapir.all ++ webjars.all ++ logging.all ++ sttp.all ++
      Seq(
        "com.beachape" %% "enumeratum" % "1.7.0",
        "io.suzaku" %% "boopickle" % "1.4.0",
        "com.lihaoyi" %% "scalatags" % "0.11.1"
      ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    wartremoverErrors ++= Warts.allBut(
      // for zio
      Wart.Any,
      Wart.Nothing
    )
  )

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
