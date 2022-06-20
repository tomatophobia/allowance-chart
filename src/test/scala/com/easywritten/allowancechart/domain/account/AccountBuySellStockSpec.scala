package com.easywritten.allowancechart.domain.account

import Assertion._
import com.easywritten.allowancechart.domain.{
  Currency,
  Holding,
  Money,
  MoneyBag,
  Nation,
  SecuritiesCompany,
  Stock,
  TransactionCost
}
import zio._
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object AccountBuySellStockSpec extends DefaultRunnableSpec {

  val apple: Stock = Stock("AAPL", Nation.USA)
  val samsung: Stock = Stock("005930", Nation.KOR)

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("AccountBuySellStockSpec")(
      suite("BuySellWithoutCost")(
        testM("Buy stock") {
          val key = AccountName("key")
          val expectedBalance: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(144.5), Currency.KRW -> Money.krw(163100)))
          val expectedHoldings: Set[Holding] =
            Set(Holding(apple, Money.usd(171.1), 5), Holding(samsung, Money.krw(69741.66667), 12))
          val expectedNetValue: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(1000), Currency.KRW -> Money.krw(1000000)))

          for {
            now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
            (accountEntity, _) <- testEntityWithProbe[
              AccountName,
              Account,
              AccountState,
              AccountEvent,
              AccountError
            ]
            account = accountEntity(key)
            _ <- account.initialize(SecuritiesCompany.Daishin)

            _ <- account.deposit(Money.krw(1000000), now)
            _ <- account.deposit(Money.usd(1000), now)

            _ <- account.buy(apple, Money.usd(167.2), 2, now)
            _ <- account.buy(apple, Money.usd(173.7), 3, now)
            _ <- account.buy(samsung, Money.krw(71200), 5, now) // Samsung Electronics
            _ <- account.buy(samsung, Money.krw(68700), 7, now)
            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            compareMoneyBag(balance, expectedBalance) &&
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
              AccountError
            ]
            account = accountEntity(key)
            _ <- account.initialize(SecuritiesCompany.Daishin)

            _ <- account.deposit(Money.usd(1000), now)
            failure <- account.buy(apple, Money.usd(167.2), 10, now).run
          } yield {
            assert(failure)(fails(equalTo(AccountError.InsufficientBalance("Buying failed"))))
          }
        },
        testM("Sell stock") {
          val key = AccountName("key")
          val expectedBalance: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(690.6), Currency.KRW -> Money.krw(708000)))
          val expectedHoldings: Set[Holding] =
            Set(Holding(apple, Money.usd(167.3), 2), Holding(samsung, Money.krw(71200), 4))
          val expectedNetValue: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(1025.2), Currency.KRW -> Money.krw(992800)))

          for {
            now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
            (accountEntity, _) <- testEntityWithProbe[
              AccountName,
              Account,
              AccountState,
              AccountEvent,
              AccountError
            ]
            account = accountEntity(key)
            _ <- account.initialize(SecuritiesCompany.Daishin)

            _ <- account.deposit(Money.krw(1000000), now)
            _ <- account.deposit(Money.usd(1000), now)
            _ <- account.buy(apple, Money.usd(167.3), 5, now)
            _ <- account.buy(samsung, Money.krw(71200), 7, now) // Samsung Electronics

            _ <- account.sell(apple, Money.usd(175.7), 3, now)
            _ <- account.sell(samsung, Money.krw(68800), 3, now)

            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            compareMoneyBag(balance, expectedBalance) &&
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
              AccountError
            ]
            account = accountEntity(key)
            _ <- account.initialize(SecuritiesCompany.Daishin)

            _ <- account.deposit(Money.usd(1000), now)
            _ <- account.buy(apple, Money.usd(167.2), 5, now)
            failure <- account.sell(apple, Money.usd(167.2), 7, now).run
          } yield {
            assert(failure)(fails(equalTo(AccountError.InsufficientShares("Selling failed"))))
          }
        },
        testM("Sell all shares") {
          val key = AccountName("key")
          val expectedBalance: MoneyBag = MoneyBag(Map(Currency.USD -> Money.usd(1000)))
          val expectedHoldings: Set[Holding] = Set()
          val expectedNetValue: MoneyBag = MoneyBag(Map(Currency.USD -> Money.usd(1000)))
          for {
            now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
            (accountEntity, _) <- testEntityWithProbe[
              AccountName,
              Account,
              AccountState,
              AccountEvent,
              AccountError
            ]
            account = accountEntity(key)
            _ <- account.initialize(SecuritiesCompany.Daishin)
            _ <- account.deposit(Money.usd(1000), now)
            _ <- account.buy(apple, Money.usd(167.2), 5, now)
            _ <- account.sell(apple, Money.usd(167.2), 5, now)

            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            compareMoneyBag(balance, expectedBalance) &&
            compareHoldings(holdings, expectedHoldings) &&
            compareMoneyBag(netValue, expectedNetValue)
          }
        }
      ),
      suite("BuySellWithCost")(
        testM("Buy and sell stock") {
          val key = AccountName("key")
          val expectedBalance: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(499.075), Currency.KRW -> Money.krw(354584.4)))
          val expectedHoldings: Set[Holding] =
            Set(Holding(apple, Money.usd(171.1), 3), Holding(samsung, Money.krw(69741.66667), 9))
          val expectedNetValue: MoneyBag =
            MoneyBag(Map(Currency.USD -> Money.usd(1012.375), Currency.KRW -> Money.krw(982259.4)))
          // 1415.6, 1.925
          for {
            now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
            (accountEntity, _) <- testEntityWithProbe[
              AccountName,
              Account,
              AccountState,
              AccountEvent,
              AccountError
            ]

            account = accountEntity(key)

            _ <- account.initialize(SecuritiesCompany.Daishin)

            _ <- account.deposit(Money.krw(1000000), now)
            _ <- account.deposit(Money.usd(1000), now)

            _ <- account.buy(apple, Money.usd(167.2), 2, now)
            _ <- account.buy(apple, Money.usd(173.7), 3, now)
            _ <- account.buy(samsung, Money.krw(71200), 5, now) // Samsung Electronics
            _ <- account.buy(samsung, Money.krw(68700), 7, now)

            _ <- account.sell(apple, Money.usd(178.25), 2, now)
            _ <- account.sell(samsung, Money.krw(64300), 3, now)

            balance <- account.balance
            holdings <- account.holdings
            netValue <- account.netValue
          } yield {
            compareMoneyBag(balance, expectedBalance) &&
            compareHoldings(holdings, expectedHoldings) &&
            compareMoneyBag(netValue, expectedNetValue)
          }
        }
      ) @@ TestAspect.ignore // TODO 수수료, 세금 적용 방식 바뀌면 없어질 테스트
    ).provideCustomLayer(TestAccountEntity.layer)
  }
}
