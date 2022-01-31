package com.easywritten.allowancechart.domain

import zio._
import zio.test.Assertion._
import zio.test._

object AccountSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] =
    suite("AccountSpec") {
      test("Deposit usd into Account") {
        val acc1 = Account()
        acc1.deposit(Money.usd(1235))
        assertTrue(acc1.balance == Money.usd(1235))
      }
    }
}
