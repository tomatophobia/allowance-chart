package com.easywritten.allowancechart.domain.account

import Assertion._
import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, TickerSymbol, TransactionCost}
import zio._
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime._
import zio.test.Assertion._
import zio.test._

object AccountBuySellStockSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = {
    suite("AccountBuySellStockSpec")(
      suite("BuySellWithoutCost")(
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
            account = accountEntity(key)
            _ <- account.initialize(TransactionCost.zero)

            _ <- account.deposit(Money.krw(1000000))
            _ <- account.deposit(Money.usd(1000))

            _ <- account.buy("AAPL", Money.usd(167.2), 2, now)
            _ <- account.buy("AAPL", Money.usd(173.7), 3, now)
            _ <- account.buy("005930", Money.krw(71200), 5, now) // Samsung Electronics
            _ <- account.buy("005930", Money.krw(68700), 7, now)
            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            assert(balance)(equalTo(expectedBalance)) &&
            compareHoldings(holdings, expectedHoldings) &&
            compareMoneyBag(netValue, expectedNetValue)
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
            account = accountEntity(key)
            _ <- account.initialize(TransactionCost.zero)

            _ <- account.deposit(Money.usd(1000))
            failure <- account.buy("AAPL", Money.usd(167.2), 10, now).run
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
            account = accountEntity(key)
            _ <- account.initialize(TransactionCost.zero)

            _ <- account.deposit(Money.krw(1000000))
            _ <- account.deposit(Money.usd(1000))
            _ <- account.buy("AAPL", Money.usd(167.3), 5, now)
            _ <- account.buy("005930", Money.krw(71200), 7, now) // Samsung Electronics

            _ <- account.sell("AAPL", Money.usd(175.7), 3, now)
            _ <- account.sell("005930", Money.krw(68800), 3, now)

            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            assert(balance)(equalTo(expectedBalance)) &&
            compareHoldings(holdings, expectedHoldings) &&
            compareMoneyBag(netValue, expectedNetValue)
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
            account = accountEntity(key)
            _ <- account.initialize(TransactionCost.zero)

            _ <- account.deposit(Money.usd(1000))
            _ <- account.buy("AAPL", Money.usd(167.2), 5, now)
            failure <- account.sell("AAPL", Money.usd(167.2), 7, now).run
          } yield {
            assert(failure)(fails(equalTo(AccountCommandReject.InsufficientShares("Selling failed"))))
          }
        }
      ),
      suite("BuySellWithCost")(
        testM("Buy and sell stock") {
          val key = AccountName("key")
          val expectedBalance: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(499.075), Currency.KRW -> Money.krw(354584.4)))
          val expectedHoldings: Map[TickerSymbol, Holding] =
            Map(
              "AAPL" -> Holding("AAPL", Money.usd(171.1), 3),
              "005930" -> Holding("005930", Money.krw(69741.66667), 9)
            )
          val expectedNetValue: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(1012.375), Currency.KRW -> Money.krw(982259.4)))
          val cost = TransactionCost(0.001, 0.003)
          // 1415.6, 1.925
          for {
            now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
            (accountEntity, _) <- testEntityWithProbe[
              AccountName,
              Account,
              AccountState,
              AccountEvent,
              AccountCommandReject
            ]

            account = accountEntity(key)

            _ <- account.initialize(cost)

            _ <- account.deposit(Money.krw(1000000))
            _ <- account.deposit(Money.usd(1000))

            _ <- account.buy("AAPL", Money.usd(167.2), 2, now)
            _ <- account.buy("AAPL", Money.usd(173.7), 3, now)
            _ <- account.buy("005930", Money.krw(71200), 5, now) // Samsung Electronics
            _ <- account.buy("005930", Money.krw(68700), 7, now)

            _ <- account.sell("AAPL", Money.usd(178.25), 2, now)
            _ <- account.sell("005930", Money.krw(64300), 3, now)

            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            compareMoneyBag(balance, expectedBalance) &&
            compareHoldings(holdings, expectedHoldings) &&
            compareMoneyBag(netValue, expectedNetValue)
          }
        }
      )
    ).provideCustomLayer(TestAccountEntity.layer)
  }
}
