package com.easywritten.allowancechart.domain.account

import Assertion._
import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, TickerSymbol, TransactionCost}
import zio._
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime._
import zio.test.Assertion._
import zio.test._

object AccountBuySellStockSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("AccountBuySellStockSpec")(
      // TODO 총 평가액의 변화가 어떻게 될지 테스트에 반영 (아마 다음 이슈에서..?)
      testM("Buy stock") {
        val key = AccountName("key")
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> Money.usd(144.5), Currency.KRW -> Money.krw(163100)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(171.1), 5), "005930" -> Holding("005930", Money.krw(69742), 12))
        val expectedNetValue: MoneyBag =
          MoneyBag(Map(Currency.USD -> Money.usd(1000), Currency.KRW -> Money.krw(1000000)))

        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity(key).initialize(TransactionCost.zero)

          _ <- accountEntity(key).deposit(Money.krw(1000000))
          _ <- accountEntity(key).deposit(Money.usd(1000))

          _ <- accountEntity(key).buy("AAPL", Money.usd(167.2), 2, now)
          _ <- accountEntity(key).buy("AAPL", Money.usd(173.7), 3, now)
          _ <- accountEntity(key).buy("005930", Money.krw(71200), 5, now) // Samsung Electronics
          _ <- accountEntity(key).buy("005930", Money.krw(68700), 7, now) // Samsung Electronics
          balance <- accountEntity(key).balance
          holdings <- accountEntity(key).holdings
          netValue <- accountEntity(key).netValue
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          compareHoldings(holdings, expectedHoldings) &&
          compareNetValue(netValue, expectedNetValue)
        }
      },
      testM("Cannot buy stock because of insufficient balance") {
        val key = AccountName("key")
        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity(key).initialize(TransactionCost.zero)

          _ <- accountEntity(key).deposit(Money.usd(1000))
          failure <- accountEntity(key).buy("AAPL", Money.usd(167.2), 10, now).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientBalance("Buying failed"))))
        }
      },
      testM("Sell stock") {
        val key = AccountName("key")
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> Money.usd(690.6), Currency.KRW -> Money.krw(708000)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(167.3), 2), "005930" -> Holding("005930", Money.krw(71200), 4))
        val expectedNetValue: MoneyBag =
          MoneyBag(Map(Currency.USD -> Money.usd(1025.2), Currency.KRW -> Money.krw(992800)))

        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity(key).initialize(TransactionCost.zero)

          _ <- accountEntity(key).deposit(Money.krw(1000000))
          _ <- accountEntity(key).deposit(Money.usd(1000))
          _ <- accountEntity(key).buy("AAPL", Money.usd(167.3), 5, now)
          _ <- accountEntity(key).buy("005930", Money.krw(71200), 7, now) // Samsung Electronics

          _ <- accountEntity(key).sell("AAPL", Money.usd(175.7), 3, now)
          _ <- accountEntity(key).sell("005930", Money.krw(68800), 3, now)

          balance <- accountEntity(key).balance
          holdings <- accountEntity(key).holdings
          netValue <- accountEntity(key).netValue
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          compareHoldings(holdings, expectedHoldings) &&
          compareNetValue(netValue, expectedNetValue)
        }
      },
      testM("Cannot sell stock because of insufficient shares") {
        val key = AccountName("key")
        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity(key).initialize(TransactionCost.zero)

          _ <- accountEntity(key).deposit(Money.usd(1000))
          _ <- accountEntity(key).buy("AAPL", Money.usd(167.2), 5, now)
          failure <- accountEntity(key).sell("AAPL", Money.usd(167.2), 7, now).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientShares("Selling failed"))))
        }
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
