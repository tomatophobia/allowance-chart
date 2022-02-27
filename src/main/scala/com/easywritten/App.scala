package com.easywritten

import cats.effect.Blocker
import cats.syntax.all._
import com.easywritten.allowancechart.adapter.in.TransactionHistoryEndpoints
import org.http4s._
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.staticcontent.{resourceServiceBuilder, webjarServiceBuilder}
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio.clock.Clock
import zio.interop.catz._
import zio._
import zio.blocking.Blocking

object App extends zio.App {

  val serverRoutes: HttpRoutes[RIO[Clock, *]] =
    ZHttp4sServerInterpreter().from(TransactionHistoryEndpoints.all).toRoutes

  // API documents
  val apiDocs: String = {
    import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
    import sttp.tapir.openapi.circe.yaml._
    OpenAPIDocsInterpreter()
      .serverEndpointsToOpenAPI(TransactionHistoryEndpoints.all, "Allowance Chart", "0.0.0")
      .toYaml
  }

  val program: RIO[ZEnv, Unit] =
    (for {
      executor <- ZManaged.fromEffect(RIO.access[Blocking](_.get.blockingExecutor))

      catsBlocker = Blocker.liftExecutionContext(executor.asEC)

      serve <- ZIO
        .runtime[ZEnv]
        .toManaged_
        .flatMap { implicit runtime =>
          EmberServerBuilder
            .default[RIO[Clock, *]]
            .withHost("localhost")
            .withPort(8080)
            .withHttpApp(
              Router(
                "/assets/webjars" -> webjarServiceBuilder[RIO[Clock, *]](catsBlocker).toRoutes,
                "/assets" -> resourceServiceBuilder[RIO[Clock, *]]("/assets", catsBlocker).toRoutes,
                "/" -> (serverRoutes <+> new SwaggerHttp4s(apiDocs).routes)
              ).orNotFound
            )
            .build
            .toManagedZIO
        }

    } yield serve).use(_ => ZIO.never)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode
}
