package com.easywritten.allowancechart.domain

import zio._
import zio.test.Assertion._
import zio.test._

object AccountSpec extends DefaultRunnableSpec {
  def spec: ZSpec[Environment, Failure] =
    suite("AccountSpec")(
      test("Deposit usd into Account") {
        val acc = Account()
        val usd = Money.usd(1235)
        acc.deposit(usd)
        val mb = MoneyBag.fromMoneys(usd)
        assertTrue(acc.balance == mb)
      },
      test("Deposit multiple times") {
        val acc = Account()
        acc.deposit(Money.usd(123.12))
        acc.deposit(Money.usd(456.45))
        acc.deposit(Money.krw(12519))
        acc.deposit(Money.krw(56947))

        val expectedMb =
          MoneyBag(
            Map[Currency, MoneyAmount](
              Currency.USD -> BigDecimal(579.57),
              Currency.KRW -> BigDecimal(69466)
            )
          )

        assertTrue(acc.balance == expectedMb)
      }
    )
}
