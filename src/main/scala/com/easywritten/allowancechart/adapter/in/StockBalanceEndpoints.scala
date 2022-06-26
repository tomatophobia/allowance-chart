package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.adapter.in.page.StockBalancePage
import com.easywritten.allowancechart.application.port.in.RegisterTransactionRecordPort
import com.easywritten.allowancechart.application.service.ServiceError
import sttp.tapir.ztapir._
import zio._

object StockBalanceEndpoints extends ErrorMapping {

  val stockBalancePage: ZServerEndpoint[Env, Unit, ServiceError, String] =
    endpoint.get
      .in("stock" / "balance")
      .out(htmlBodyUtf8)
      .errorOut(customErrorBody())
      .tag(ApiDocTag.stockStatistics)
      .summary("Stock Balance Page")
      .zServerLogic { _ =>
        ZIO.succeed(StockBalancePage.html)
      }

  val all: List[ZServerEndpoint[Env, _, _, _]] =
    List(
      stockBalancePage
    )

  // TODO Env가 모두 통일되어 있어야 하는데 엔드포인트가 필요한 서비스만 표시할 수는 없나...
  type Env = Has[RegisterTransactionRecordPort]

}
