package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.account.Assertion.{compareHoldings, compareMoneyBag}
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Nation, SecuritiesCompany, Stock}
import zio._
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime.testEntityWithProbe
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object AccountDividendSpec extends DefaultRunnableSpec {

  val apple: Stock = Stock("AAPL", Nation.USA)

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("AccountDividendSpec")(
      testM("Dividend event on account") {
        val key = AccountName("key")
        val expectedBalance: MoneyBag = MoneyBag.fromMoneys(Money.usd(802.54))
        val expectedHoldings: Set[Holding] = Set(Holding(apple, Money.usd(100), 2))
        val expectedNetValue: MoneyBag = MoneyBag.fromMoneys(Money.usd(1002.54))
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
          _ <- account.initialize(SecuritiesCompany.Daishin)
          _ <- account.deposit(Money.usd(1000), now)
          _ <- account.buy(apple, Money.usd(100), 2, now)
          _ <- account.dividendPaid(apple, Money.usd(2.65), Money.usd(0.11), now)
          balance <- account.balance
          holdings <- account.holdings
          netValue <- account.netValue
        } yield {
          compareMoneyBag(balance, expectedBalance) &&
          compareHoldings(holdings, expectedHoldings) &&
          compareMoneyBag(netValue, expectedNetValue)
        }
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
