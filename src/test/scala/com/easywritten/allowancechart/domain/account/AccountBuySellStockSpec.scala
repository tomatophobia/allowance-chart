package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, TickerSymbol}
import zio._
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core._
import zio.entity.test.TestEntityRuntime._
import zio.entity.test.TestMemoryStores
import zio.test.Assertion._
import zio.test._

object AccountBuySellStockSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = {
    suite("AccountBuySellStockSpec")(
      // TODO 총 평가액의 변화가 어떻게 될지 테스트에 반영 (아마 다음 이슈에서..?)
      testM("Buy stock") {
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> BigDecimal(144.5), Currency.KRW -> BigDecimal(163100)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(171.1), 5), "005930" -> Holding("005930", Money.krw(69742), 12))

        (for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.krw(1000000))
          _ <- accountEntity("key").deposit(Money.usd(1000))

          _ <- accountEntity("key").buy("AAPL", Money.usd(167.2), 2, now)
          _ <- accountEntity("key").buy("AAPL", Money.usd(173.7), 3, now)
          _ <- accountEntity("key").buy("005930", Money.krw(71200), 5, now) // Samsung Electronics
          _ <- accountEntity("key").buy("005930", Money.krw(68700), 7, now) // Samsung Electronics
          balance <- accountEntity("key").balance
          holdings <- accountEntity("key").holdings
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          compareHoldings(holdings, expectedHoldings)
        }).provideSomeLayer[Environment](layer)
      },
      testM("Cannot buy stock because of insufficient balance") {
        (for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.usd(1000))
          failure <- accountEntity("key").buy("AAPL", Money.usd(167.2), 10, now).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientBalance("Buying failed"))))
        }).provideSomeLayer[Environment](layer)
      },
      testM("Sell stock") {
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> BigDecimal(690.6), Currency.KRW -> BigDecimal(708000)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(167.3), 2), "005930" -> Holding("005930", Money.krw(71200), 4))

        (for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.krw(1000000))
          _ <- accountEntity("key").deposit(Money.usd(1000))
          _ <- accountEntity("key").buy("AAPL", Money.usd(167.3), 5, now)
          _ <- accountEntity("key").buy("005930", Money.krw(71200), 7, now) // Samsung Electronics

          _ <- accountEntity("key").sell("AAPL", Money.usd(175.7), 3, now)
          _ <- accountEntity("key").sell("005930", Money.krw(68800), 3, now)

          balance <- accountEntity("key").balance
          holdings <- accountEntity("key").holdings
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          assert(holdings)(equalTo(expectedHoldings))
        }).provideSomeLayer[Environment](layer)
      },
      testM("Cannot sell stock because of insufficient shares") {
        (for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.usd(1000))
          _ <- accountEntity("key").buy("AAPL", Money.usd(167.2), 5, now)
          failure <- accountEntity("key").sell("AAPL", Money.usd(167.2), 7, now).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientShares("Selling failed"))))
        }).provideSomeLayer[Environment](layer)
      }
    )
  }

  import EventSourcedAccount.accountProtocol

  private val layer = Clock.any ++ TestMemoryStores.make[String, AccountEvent, AccountState](50.millis) >>>
    testEntity(
      EventSourcedAccount.tagging,
      EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountCommandReject](
        new EventSourcedAccount(_),
        EventSourcedAccount.eventHandlerLogic,
        AccountCommandReject.FromThrowable
      )
    )

  /** holdings의 holding들을 각각 비교한다. 무한소수인 경우, 통화별로 정해진 소수점 자릿수에서 반올림하여 비교한다.
    */
  private def compareHoldings(
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
}
