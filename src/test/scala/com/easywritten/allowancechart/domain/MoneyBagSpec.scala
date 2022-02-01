package com.easywritten.allowancechart.domain

import zio._
import zio.test.Assertion._
import zio.test._

object MoneyBagSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("MoneyBagSpec") {
      test("MoneyBag can merge several Money instance") {
        val m1 = Money(Currency.USD, 123.35)
        val m2 = Money(Currency.USD, 247.98)
        val m3 = Money(Currency.KRW, 159421)
        val m4 = Money(Currency.KRW, 592310)

        val beforeMoneyBag = MoneyBag(Map())

        val afterMoneyBag = beforeMoneyBag + m1 + m2 + m3 + m4

        assertTrue(afterMoneyBag.moneys(Currency.USD: Currency) == BigDecimal(371.33)) &&
          assertTrue(afterMoneyBag.moneys(Currency.KRW: Currency) == BigDecimal(751731))
      }
    }
}
