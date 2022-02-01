package com.easywritten.allowancechart.domain

import zio._
import zio.test.Assertion._
import zio.test._

object AccountSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] =
    suite("AccountSpec") {
      test("Deposit usd into Account") {
        val acc1 = Account()
        val usd = Money.usd(1235)
        acc1.deposit(usd)
        val mb = MoneyBag.fromMoneys(usd)
        assertTrue(acc1.balance == mb)
      }
    }
}
