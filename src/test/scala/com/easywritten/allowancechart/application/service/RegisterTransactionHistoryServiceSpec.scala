package com.easywritten.allowancechart.application.service

import zio._
import zio.test._
import zio.test.Assertion._

object RegisterTransactionHistoryServiceSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("RegisterTransactionHistoryServiceSpec")(
    testM("publish account events using transaction history data")(
      assertCompletesM
    ),
    testM("save transaction history data to repository")(
      assertCompletesM
    )
  )
}
