import sbt._

object Dependencies {

  object zio {
    private val version = "1.0.13"
    val zio: ModuleID = "dev.zio" %% "zio" % version
    val zioTest: ModuleID = "dev.zio" %% "zio-test" % version % Test
    val testSbt: ModuleID = "dev.zio" %% "zio-test-sbt" % version % Test
    val testMagnolia: ModuleID = "dev.zio" %% "zio-test-magnolia" % version % Test
    val testIntellij: ModuleID = "dev.zio" %% "zio-test-intellij" % version % Test

    object entity {
      private val version = "0.0.26"
      val core = "io.github.thehonesttech" %% "zio-entity-core" % version
      val akkaruntime = "io.github.thehonesttech" %% "zio-entity-akkaruntime" % version
      val postgres = "io.github.thehonesttech" %% "zio-entity-postgres" % version
      val all: Seq[ModuleID] = Seq(core, akkaruntime, postgres)
    }

    val all: Seq[ModuleID] = Seq(zio, zioTest, testSbt, testMagnolia, testIntellij) ++ entity.all
  }

  object cats {
    private val version = "2.7.0"
    val core = "org.typelevel" %% "cats-core" % version
    val all: Seq[ModuleID] = Seq(core)
  }

  object http4s {
    private val version = "0.22.11"
    val core = "org.http4s" %% "http4s-core" % version
    val dsl = "org.http4s" %% "http4s-dsl" % version
    val emberServer = "org.http4s" %% "http4s-ember-server" % version
    val emberClient = "org.http4s" %% "http4s-ember-client" % version
    val all: Seq[ModuleID] = Seq(core, dsl, emberServer, emberClient)
  }

  object tapir {
    val core = "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.18.3"
    val jsonCirce = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.18.3"
    val openapiDocs = "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "0.18.3"
    val openapiCirceYaml = "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.18.3"
    val http4sServer = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.18.3"
    val swaggerUiHttp4s = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.18.3"
    val zioHttp4sServer = "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.18.3"
    val all: Seq[ModuleID] =
      Seq(core, jsonCirce, openapiDocs, openapiCirceYaml, http4sServer, swaggerUiHttp4s, zioHttp4sServer)
  }

  object webjars {
    val adminLTE = "org.webjars" % "AdminLTE" % "3.2.0"
    val plotly = "org.webjars.npm" % "plotly.js-dist-min" % "2.9.0"
    val all: Seq[ModuleID] = Seq(adminLTE, plotly)
  }

  // tapir에서 사용하는 로깅
  object logging {
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.10"
    val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.10"
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    val all: Seq[ModuleID] = Seq(logbackClassic, logbackCore, scalaLogging)
  }

}
