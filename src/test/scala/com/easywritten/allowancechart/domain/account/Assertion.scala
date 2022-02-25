package com.easywritten.allowancechart.domain.account

import cats.implicits._
import com.easywritten.allowancechart.domain.{Holding, MoneyBag, Ticker}
import zio.test._
import zio.test.Assertion._

object Assertion {

  /** holdings의 holding들을 각각 비교한다. 무한소수인 경우, 통화별로 정해진 소수점 자릿수에서 반올림하여 비교한다.
    */
  def compareHoldings(
      actual: Set[Holding],
      expected: Set[Holding]
  ): TestResult = {
    if (actual.size === expected.size) {
      val zipped = (for {
        x1 <- actual
        x2 <- expected
      } yield if (x1.stock === x2.stock) Some((x1, x2)) else None).collect { case Some(v) => v }

      if (zipped.size === expected.size) {
        zipped.foldLeft(assertCompletes) { case (acc, (h1, h2)) =>
          val m1 = h1.unitPrice.halfUp
          val q1 = h1.quantity

          val m2 = h2.unitPrice.halfUp
          val q2 = h2.quantity

          acc && assert(m1)(equalTo(m2)) && assert(q1)(equalTo(q2))
        }
      } else assertCompletes.negate
    } else assertCompletes.negate
  }

  /** MoneyBag 비교 시 반올림하여 비교
    */
  def compareMoneyBag(
      actual: MoneyBag,
      expected: MoneyBag
  ): TestResult = assert(actual.halfUpAll)(equalTo(expected.halfUpAll))
}
