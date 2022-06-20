package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.account.Assertion.compareMoneyBag
import com.easywritten.allowancechart.domain.{Money, MoneyBag, SecuritiesCompany}
import zio._
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime.testEntityWithProbe
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object AccountForeignExchangeSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("AccountForeignExchangeSpec")(
      testM("Foreign exchange buy") {
        val key = AccountName("key")
        val expectedBalance = MoneyBag.fromMoneys(Money.krw(5), Money.usd(434.62))
        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, probe) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountError
          ]
          account = accountEntity(key)
          _ <- account.initialize(SecuritiesCompany.Daishin)
          _ <- account.deposit(Money.krw(500000), now)
          _ <- account.foreignExchangeBuy(MoneyBag.fromMoneys(Money.krw(-499995), Money.usd(434.62)), 1150.42, now)
          balance <- account.balance
        } yield compareMoneyBag(balance, expectedBalance)
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
