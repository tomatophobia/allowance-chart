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

    object interop {
      val cats = "dev.zio" %% "zio-interop-cats" % "2.4.1.0"
      val all: Seq[ModuleID] = Seq(cats)
    }

    object logging {
      private val version = "0.5.14"
      val core = "dev.zio" %% "zio-logging" % version
      val slf4j = "dev.zio" %% "zio-logging-slf4j" % version
      val all: Seq[ModuleID] = Seq(core, slf4j)
    }

    val all: Seq[ModuleID] =
      Seq(zio, zioTest, testSbt, testMagnolia, testIntellij) ++ entity.all ++ interop.all ++ logging.all
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
    private val oldVersion = "0.18.3"
    private val version = "0.18.3"
    val core = "com.softwaremill.sttp.tapir" %% "tapir-core" % version
    val jsonCirce = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % version
    val openapiDocs = "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % version
    val openapiCirceYaml = "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % version
    val http4sServer = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % version
    val swaggerUiHttp4s = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % oldVersion
    val zioHttp4sServer = "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % version
    val sttpStubServer = "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % version
    val all: Seq[ModuleID] =
      Seq(
        core,
        jsonCirce,
        openapiDocs,
        openapiCirceYaml,
        http4sServer,
        swaggerUiHttp4s,
        zioHttp4sServer,
        sttpStubServer
      )
  }

  object sttp {
    private val version = "3.5.0"
    val core = "com.softwaremill.sttp.client3" %% "core" % version
    val javaHttpClientBackendZio = "com.softwaremill.sttp.client3" %% "httpclient-backend-zio1" % version
    val asyncHttpClientBackendZio = "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio1" % version
    val armeriaBackendZio = "com.softwaremill.sttp.client3" %% "armeria-backend-zio1" % version
    val all: Seq[ModuleID] = Seq(core, javaHttpClientBackendZio, asyncHttpClientBackendZio, armeriaBackendZio)
  }

  object circe {
    private val version = "0.14.1"
    val core = "io.circe" %% "circe-core" % version
    val generic = "io.circe" %% "circe-generic" % version
    val genericExtras = "io.circe" %% "circe-generic-extras" % version
    val parser = "io.circe" %% "circe-parser" % version
    val yaml = "io.circe" %% "circe-yaml" % version
    val all: Seq[ModuleID] = Seq(core, generic, genericExtras, parser, yaml)
  }

  object webjars {
    val adminLTE = "org.webjars" % "AdminLTE" % "3.2.0"
    val plotly = "org.webjars.npm" % "plotly.js-dist-min" % "2.9.0"
    val dataTables = "org.webjars" % "datatables" % "1.11.4"
    val all: Seq[ModuleID] = Seq(adminLTE, plotly, dataTables)
  }

  // tapir에서 사용하는 로깅
  object tapirLogging {
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.10"
    val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.10"
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    val all: Seq[ModuleID] = Seq(logbackClassic, logbackCore, scalaLogging)
  }

}
