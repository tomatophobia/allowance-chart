package com.easywritten.allowancechart.domain

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object StockSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] = suite("StockSpec")(
    test("Stock has symbol, market") {
      val apple = Stock("AAPL", Nation.USA)
      val samsung = Stock("005930", Nation.KOR)

      assert(apple.symbol)(equalTo("AAPL")) &&
      assert(apple.nation)(equalTo(Nation.USA)) &&
      assert(samsung.symbol)(equalTo("005930")) &&
      assert(samsung.nation)(equalTo(Nation.KOR))
    },
    test("Stock equality and inequality") {
      val apple1 = Stock("AAPL", Nation.USA)
      val apple2 = Stock("AAPL", Nation.USA)
      val samsung = Stock("005930", Nation.KOR)

      assert(apple1)(equalTo(apple2)) &&
      assert(apple1)(not(equalTo(samsung)))
    }
  )
}
