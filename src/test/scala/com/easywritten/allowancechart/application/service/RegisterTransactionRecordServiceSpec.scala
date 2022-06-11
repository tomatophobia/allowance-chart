package com.easywritten.allowancechart.application.service

import zio._
import zio.test._
import zio.test.Assertion._

object RegisterTransactionRecordServiceSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("RegisterTransactionRecordServiceSpec")(
    testM("publish account events using transaction record data")(
      assertCompletesM
    ),
    testM("save transaction record data to repository")(
      assertCompletesM
    )
  )
}
