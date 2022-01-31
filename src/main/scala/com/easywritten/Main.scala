package com.easywritten

import zio.console.putStrLn
import zio.{App, ExitCode, URIO}

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    putStrLn("Welcome to your first ZIO app!").exitCode
}
