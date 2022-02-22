package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, MoneyBag, TickerSymbol}
import zio.test.Assertion.equalTo
import zio.test.{TestResult, assert, assertCompletes}

object Assertion {

  /** holdings의 holding들을 각각 비교한다. 무한소수인 경우, 통화별로 정해진 소수점 자릿수에서 반올림하여 비교한다.
    */
  def compareHoldings(
      actual: Map[TickerSymbol, Holding],
      expected: Map[TickerSymbol, Holding]
  ): TestResult = {
    val keys = actual.keys ++ expected.keys
    keys.foldLeft(assertCompletes) { (asserts, symbol) =>
      val h1 = actual.get(symbol)
      val m1 = h1.map(_.averagePrice.halfUp)
      val q1 = h1.map(_.quantity)

      val h2 = expected.get(symbol)
      val m2 = h2.map(_.averagePrice.halfUp)
      val q2 = h2.map(_.quantity)

      asserts && assert(m1)(equalTo(m2)) && assert(q1)(equalTo(q2))
    }
  }

  /** MoneyBag 비교 시 반올림하여 비교
    */
  def compareMoneyBag(
      actual: MoneyBag,
      expected: MoneyBag
  ): TestResult = assert(actual.halfUpAll)(equalTo(expected.halfUpAll))
}
