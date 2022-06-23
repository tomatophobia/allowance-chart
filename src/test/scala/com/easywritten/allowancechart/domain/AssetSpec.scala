package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.domain.account.Assertion.{compareHoldings, compareMoneyBag}
import com.easywritten.allowancechart.domain.account.{AccountName, TestAccountEntity}
import com.easywritten.allowancechart.testLogLayer
import zio._
import zio.clock.Clock
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object AssetSpec extends DefaultRunnableSpec {

  val apple: Stock = Stock("AAPL", Nation.USA)
  val samsung: Stock = Stock("005930", Nation.KOR)

  override def spec: ZSpec[TestEnvironment, Any] = suite("AssetSpec")(
    testM("Asset can be changed by adding transaction information") {
      val accountName1 = AccountName("대신증권")
      val accountName2 = AccountName("NH투자증권")

      val expectedBalance1: MoneyBag =
        MoneyBag(Map(Currency.USD -> Money.usd(55.46)))
      val expectedHoldings1: Set[Holding] =
        Set(Holding(apple, Money.usd(32.23), 1))
      val expectedNetValue1: MoneyBag =
        MoneyBag(Map(Currency.USD -> Money.usd(87.69)))

      val expectedBalance2: MoneyBag =
        MoneyBag(Map(Currency.KRW -> Money.krw(96261)))
      val expectedHoldings2: Set[Holding] =
        Set(Holding(samsung, Money.krw(78240), 2))
      val expectedNetValue2: MoneyBag =
        MoneyBag(Map(Currency.KRW -> Money.krw(252741)))

      for {
        now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
        asset <- ZIO.service[Asset.Service]
        _ <- asset.initialize(accountName1, SecuritiesCompany.Daishin)
        _ <- asset.initialize(accountName2, SecuritiesCompany.Daishin)

        _ <- asset.deposit(accountName1, Money.usd(123.45), now)
        _ <- asset.buy(accountName1, apple, Money.usd(32.23), 2, now)
        _ <- asset.sell(accountName1, apple, Money.usd(47.79), 1, now)
        _ <- asset.withdraw(accountName1, Money.usd(51.32), now)
        balance1 <- asset.balance(accountName1)
        holdings1 <- asset.holdings(accountName1)
        netValue1 <- asset.netValue(accountName1)

        _ <- asset.deposit(accountName2, Money.krw(300000), now)
        _ <- asset.buy(accountName2, samsung, Money.krw(78240), 3, now)
        _ <- asset.sell(accountName2, samsung, Money.krw(65490), 1, now)
        _ <- asset.withdraw(accountName2, Money.krw(34509), now)
        balance2 <- asset.balance(accountName2)
        holdings2 <- asset.holdings(accountName2)
        netValue2 <- asset.netValue(accountName2)
      } yield compareMoneyBag(balance1, expectedBalance1) &&
        compareHoldings(holdings1, expectedHoldings1) &&
        compareMoneyBag(netValue1, expectedNetValue1) &&
        compareMoneyBag(balance2, expectedBalance2) &&
        compareHoldings(holdings2, expectedHoldings2) &&
        compareMoneyBag(netValue2, expectedNetValue2)
    }
  ).provideCustomLayer((testLogLayer and TestAccountEntity.layer) to Asset.layer)
}
