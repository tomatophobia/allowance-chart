package com.easywritten.allowancechart.domain

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

import scala.util.Try

object MoneySpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("MoneySpec")(
      test("scale") {
        val m1 = Money.krw(5.5)
        val m2 = Money.krw(6.5)

        val e1 = Money.krw(5)
        val e2 = Money.krw(6)
        val e3 = Money.krw(7)

        assert(m1.floor)(equalTo(e1)) &&
        assert(m1.ceiling)(equalTo(e2)) &&
        assert(m1.halfUp)(equalTo(e2)) &&
        assert(m1.halfEven)(equalTo(e2)) &&
        assert(m2.floor)(equalTo(e2)) &&
        assert(m2.ceiling)(equalTo(e3)) &&
        assert(m2.halfUp)(equalTo(e3)) &&
        assert(m2.halfEven)(equalTo(e2))
      },
      test("unsafe + -") {
        val m1 = Money.krw(42)
        val m2 = Money.krw(27)
        assert(m1 unsafe_+ m2)(equalTo(Money.krw(69))) &&
        assert(m1 unsafe_- m2)(equalTo(Money.krw(15))) &&
        assert(m2 unsafe_- m1)(equalTo(Money.krw(-15)))
      },
      test("unsafe opeation may fail") {
        val m1 = Money.krw(42)
        val m2 = Money.usd(42)
        assert(Try(m1 unsafe_+ m2))(isFailure) &&
        assert(Try(m1 unsafe_- m2))(isFailure)
      },
      test("* / with BigDecimal") {
        val m = Money.krw(325)
        assert(m * 2)(equalTo(Money.krw(650))) &&
        assert(m / 2)(equalTo(Money.krw(162.5)))
      }
    )
}
