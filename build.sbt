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
      "com.lihaoyi" %% "scalatags" % "0.11.1",
      "org.http4s" %% "http4s-core" % "0.22.11",
      "org.http4s" %% "http4s-dsl" % "0.22.11",
      "org.http4s" %% "http4s-ember-server" % "0.22.11",
      "org.http4s" %% "http4s-ember-client" % "0.22.11",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.18.3",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.18.3",
      // tapir에서 사용하는 로깅
      "ch.qos.logback" % "logback-classic" % "1.2.10",
      "ch.qos.logback" % "logback-core" % "1.2.10",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    wartremoverErrors ++= Warts.allBut(
      // for zio
      Wart.Any,
      Wart.Nothing
    )
  )

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
