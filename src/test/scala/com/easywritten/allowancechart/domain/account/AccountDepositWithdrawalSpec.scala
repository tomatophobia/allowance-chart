package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.account.Assertion.compareMoneyBag
import com.easywritten.allowancechart.domain.{Currency, Money, MoneyBag, SecuritiesCompany, TransactionCost}
import zio.ZIO
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object AccountDepositWithdrawalSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("AccountDepositWithdrawalSpec")(
      testM("Deposit money into Account several times") {
        val key = AccountName("key")

        val moneys: List[Money] = List(Money.usd(123.12), Money.usd(456.45), Money.krw(12519), Money.krw(56947))

        val expectedBalance: MoneyBag = moneys.foldLeft(MoneyBag.empty)(_.add(_))

        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, probe) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          account = accountEntity(key)
          _ <- account.initialize(SecuritiesCompany.Daishin)

          _ <- account.deposit(moneys(0), now)
          _ <- account.deposit(moneys(1), now)
          _ <- account.deposit(moneys(2), now)
          _ <- account.deposit(moneys(3), now)
          events <- probe.probeForKey(key).events
          balance <- account.balance
          netValue <- account.netValue
        } yield {
          assert(events)(
            equalTo(
              AccountEvent.Initialize(SecuritiesCompany.Daishin) :: moneys.map[AccountEvent](m =>
                AccountEvent.Deposit(m, now)
              )
            )
          ) &&
          compareMoneyBag(balance, expectedBalance) &&
          compareMoneyBag(netValue, expectedBalance)
        }
      },
      testM("Withdraw money from account several times") {
        val key = AccountName("key")

        val expectedBalance = MoneyBag(Map(Currency.USD -> Money.usd(420.43), Currency.KRW -> Money.krw(298096)))

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
          _ <- account.deposit(Money.krw(1000000), now)

          _ <- account.withdraw(Money.usd(123.12), now)
          _ <- account.withdraw(Money.usd(456.45), now)
          _ <- account.withdraw(Money.krw(232579), now)
          _ <- account.withdraw(Money.krw(469325), now)
          balance <- account.balance
          netValue <- account.netValue
        } yield {
          compareMoneyBag(balance, expectedBalance) &&
          compareMoneyBag(netValue, expectedBalance)
        }
      },
      testM("Cannot withdraw money when there is not enough balance") {
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
          _ <- account.initialize(SecuritiesCompany.Daishin)

          _ <- account.deposit(Money.usd(100), now)
          failure <- account.withdraw(Money.usd(123.12), now).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientBalance("Withdrawal failed"))))
        }
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
