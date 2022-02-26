package com.easywritten

import cats.syntax.all._
import com.easywritten.allowancechart.adapter.in.PetEndpoints
import io.circe.generic.auto._
import org.http4s._
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.ztapir._
import zio.clock.Clock
import zio.interop.catz._
import zio._

object App extends zio.App {

  val serverRoutes: HttpRoutes[RIO[Clock, *]] = ZHttp4sServerInterpreter().from(PetEndpoints.all).toRoutes

  // API documents
  val apiDocs: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(PetEndpoints.all, "Our pets", "1.0").toYaml
  }

  val serve: RIO[ZEnv, Unit] =
    ZIO
      .runtime[ZEnv]
      .toManaged_
      .flatMap { implicit runtime =>
        EmberServerBuilder
          .default[RIO[Clock, *]]
          .withHost("localhost")
          .withPort(8080)
          .withHttpApp(Router("/" -> (serverRoutes <+> new SwaggerHttp4s(apiDocs).routes)).orNotFound)
          .build
          .toManagedZIO
      }
      .use(_ => ZIO.never)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = serve.exitCode
}
