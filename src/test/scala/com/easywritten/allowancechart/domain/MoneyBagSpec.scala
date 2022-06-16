package com.easywritten.allowancechart.domain

import zio._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object MoneyBagSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("MoneyBagSpec")(
      test("MoneyBag can merge several Money instance") {
        val m1 = Money(Currency.USD, 123.35)
        val m2 = Money(Currency.USD, 247.98)
        val m3 = Money(Currency.KRW, 159421)
        val m4 = Money(Currency.KRW, 592310)

        val beforeMoneyBag = MoneyBag(Map())

        val afterMoneyBag = beforeMoneyBag.add(m1).add(m2).add(m3).add(m4)
        val afterMoneyBag2 = beforeMoneyBag.add(m1).subtract(m2).subtract(m3).add(m4)

        assert(afterMoneyBag.moneys(Currency.USD: Currency))(equalTo(Money.usd(371.33))) &&
        assert(afterMoneyBag.moneys(Currency.KRW: Currency))(equalTo(Money.krw(751731))) &&
        assert(afterMoneyBag2.moneys(Currency.USD: Currency))(equalTo(Money.usd(-124.63))) &&
        assert(afterMoneyBag2.moneys(Currency.KRW: Currency))(equalTo(Money.krw(432889)))
      },
      test("MoneyBag + -") {
        val mb1 = MoneyBag.fromMoneys(Money.krw(151500), Money.usd(-2450.35))
        val mb2 = MoneyBag.fromMoneys(Money.krw(-94205), Money.usd(4921.24))
        assert(mb1 + mb2)(equalTo(MoneyBag.fromMoneys(Money.krw(57295), Money.usd(2470.89)))) &&
        assert(mb1 - mb2)(equalTo(MoneyBag.fromMoneys(Money.krw(245705), Money.usd(-7371.59))))
      }
    )
}
