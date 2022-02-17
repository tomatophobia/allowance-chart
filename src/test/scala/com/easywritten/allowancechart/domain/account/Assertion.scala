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
      val m1 = actual.get(symbol).map(_.averagePrice.halfUp)
      val m2 = expected.get(symbol).map(_.averagePrice.halfUp)
      asserts && assert(m1)(equalTo(m2))
    }
  }

  def compareNetValue(
      actual: MoneyBag,
      expected: MoneyBag
  ): TestResult = assert(actual.halfUpAll)(equalTo(expected.halfUpAll))
}
