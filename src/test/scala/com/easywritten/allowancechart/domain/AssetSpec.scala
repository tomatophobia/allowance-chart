package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.domain.account.Assertion.{compareHoldings, compareMoneyBag}
import com.easywritten.allowancechart.domain.account.AccountName
import zio._
import zio.clock.Clock
import zio.test._
import zio.test.Assertion._

object AssetSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("AssetSpec")(
    testM("Asset can be changed by adding transaction information") {
      val accountName1 = AccountName("대신증권")
      val accountName2 = AccountName("NH투자증권")

      val expectedBalance1: MoneyBag =
        MoneyBag(Map(Currency.USD -> Money.usd(55.46)))
      val expectedHoldings1: Map[Ticker, Holding] =
        Map("AAPL" -> Holding("AAPL", Money.usd(32.23), 1))
      val expectedNetValue1: MoneyBag =
        MoneyBag(Map(Currency.USD -> Money.usd(87.69)))

      val expectedBalance2: MoneyBag =
        MoneyBag(Map(Currency.KRW -> Money.krw(96261)))
      val expectedHoldings2: Map[Ticker, Holding] =
        Map("005930" -> Holding("005930", Money.krw(78240), 2))
      val expectedNetValue2: MoneyBag =
        MoneyBag(Map(Currency.KRW -> Money.krw(252741)))

      for {
        now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
        asset <- ZIO.service[Asset]
        acc1 = asset.accounts(accountName1)
        acc2 = asset.accounts(accountName2)
        _ <- acc1.initialize(TransactionCost.zero)
        _ <- acc2.initialize(TransactionCost.zero)

        _ <- acc1.deposit(Money.usd(123.45))
        _ <- acc1.buy("AAPL", Money.usd(32.23), 2, now)
        _ <- acc1.sell("AAPL", Money.usd(47.79), 1, now)
        _ <- acc1.withdraw(Money.usd(51.32))
        balance1 <- acc1.balance
        holdings1 <- acc1.holdings
        netValue1 <- acc1.netValue

        _ <- acc2.deposit(Money.krw(300000))
        _ <- acc2.buy("005930", Money.krw(78240), 3, now)
        _ <- acc2.sell("005930", Money.krw(65490), 1, now)
        _ <- acc2.withdraw(Money.krw(34509))
        balance2 <- acc2.balance
        holdings2 <- acc2.holdings
        netValue2 <- acc2.netValue
      } yield assert(balance1)(equalTo(expectedBalance1)) &&
        compareHoldings(holdings1, expectedHoldings1) &&
        compareMoneyBag(netValue1, expectedNetValue1) &&
        assert(balance2)(equalTo(expectedBalance2)) &&
        compareHoldings(holdings2, expectedHoldings2) &&
        compareMoneyBag(netValue2, expectedNetValue2)
    }
  ).provideCustomLayer(TestAsset.layer)
}
