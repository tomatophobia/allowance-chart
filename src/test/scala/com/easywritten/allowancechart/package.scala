package com.easywritten

import zio.URLayer
import zio.clock.Clock
import zio.console.Console
import zio.logging.{LogLevel, Logging}

package object allowancechart {
  val testLogLayer: URLayer[Console with Clock, Logging] =
    Logging.console(logLevel = LogLevel.Error) to Logging.withRootLoggerName("test-allowance-chart")
}
