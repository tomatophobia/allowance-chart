package com.easywritten

import zio.URLayer
import zio.clock.Clock
import zio.console.Console
import zio.logging._

package object allowancechart {
  val testLogLayer: URLayer[Console with Clock, Logging] =
    Logging.console(
      logLevel = LogLevel.Debug,
      format = LogFormat.ColoredLogFormat { (ctx, line) =>
        ctx.renderContext
          .collect {
            case (annotation, value) if annotation != "timestamp" && annotation != "name" && annotation != "level" =>
              s"[$annotation: $value]"
          }
          .mkString(" ") + " " + line
      }
    ) to Logging.withRootLoggerName("test-allowance-chart")
}
