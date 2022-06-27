package com

import com.easywritten.allowancechart.adapter.in.{StockBalanceEndpoints, TransactionRecordEndpoints}
import com.easywritten.allowancechart.application.port.in.RegisterTransactionRecordPort
import com.easywritten.allowancechart.application.service.RegisterTransactionRecordService
import com.easywritten.allowancechart.domain.Asset
import com.easywritten.allowancechart.domain.account.AccountCommandHandler
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.logging._

package object easywritten {
  // TODO EndpointEnv가 적절한 이름이 아닌 것 같기도
  type ClockWithEndpointEnv = Clock with EndpointEnv
  type EndpointEnv = TransactionRecordEndpoints.Env with StockBalanceEndpoints.Env

  val logLayer: URLayer[Console with Clock, Logging] =
    Logging.console(
      logLevel = LogLevel.Info,
      format = LogFormat.ColoredLogFormat { (ctx, line) =>
        ctx.renderContext
          .collect {
            case (annotation, value) if annotation != "timestamp" && annotation != "name" && annotation != "level" =>
              s"[$annotation: $value]"
          }
          .mkString(" ") + " " + line
      }
    ) to Logging.withRootLoggerName("allowance-chart")

  // TODO 컴포넌트 많아지면 type alias들 추가하기
  val domainLayers: RLayer[ZEnv with Logging, Has[Asset.Service]] =
    ((Logging.any and AccountCommandHandler.accounts) to Asset.layer)

  val applicationServiceLayers: URLayer[Has[Asset.Service] with Logging, Has[RegisterTransactionRecordPort]] =
    RegisterTransactionRecordService.layer

  val appLayers
      : ZLayer[ZEnv, Throwable, ZEnv with Logging with Has[Asset.Service] with Has[RegisterTransactionRecordPort]] =
    ZEnv.any andTo logLayer andTo domainLayers andTo applicationServiceLayers

}
