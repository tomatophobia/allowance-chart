package com.easywritten

import com.easywritten.allowancechart.domain._
import zio.console.putStrLn
import zio.entity.core._
import zio.entity.core.Fold._
import zio._
import zio.clock.Clock
import zio.duration.durationInt

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    putStrLn("Welcome to your first ZIO app!").exitCode
}
