package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.adapter.in.page.StockBalancePage
import com.easywritten.allowancechart.application.service.ServiceError
import sttp.tapir.ztapir._
import zio._

trait StockBalanceEndpoints[R <: StockBalanceEndpoints.Env] extends ErrorMapping {

  val stockBalancePage: ZServerEndpoint[R, Unit, ServiceError, String] =
    endpoint.get
      .in("stock" / "balance")
      .out(htmlBodyUtf8)
      .errorOut(customErrorBody())
      .tag(ApiDocTag.stockStatistics)
      .summary("Stock Balance Page")
      .zServerLogic { _ =>
        ZIO.succeed(StockBalancePage.html)
      }

  val stockBalanceEndpoints: List[ZServerEndpoint[R, _, _, _]] =
    List(
      stockBalancePage
    )

}

object StockBalanceEndpoints {
  type Env = Any
}
