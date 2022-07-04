package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.EndpointEnv
import sttp.tapir.ztapir.ZServerEndpoint

final class EndpointsLive[R <: EndpointEnv]
    extends TransactionRecordEndpoints[R]
    with StockBalanceEndpoints[R] {

  val live: List[ZServerEndpoint[R, _, _, _]] =
    transactionRecordEndpoints ::: stockBalanceEndpoints
}
