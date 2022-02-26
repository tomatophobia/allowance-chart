ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.easywritten"
ThisBuild / organizationName := "easywritten"

lazy val root = (project in file("."))
  .settings(
    name := "allowance-chart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.13",
      "dev.zio" %% "zio-test" % "1.0.13" % Test,
      "dev.zio" %% "zio-test-sbt" % "1.0.13" % Test,
      "dev.zio" %% "zio-test-magnolia" % "1.0.13" % Test,
      "org.typelevel" %% "cats-core" % "2.7.0",
      "com.beachape" %% "enumeratum" % "1.7.0",
      "io.github.thehonesttech" %% "zio-entity-core" % "0.0.26",
      "io.github.thehonesttech" %% "zio-entity-akkaruntime" % "0.0.26",
      "io.github.thehonesttech" %% "zio-entity-postgres" % "0.0.26",
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
