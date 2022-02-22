package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Currency, Money, MoneyBag, TransactionCost}
import zio.entity.test.TestEntityRuntime._
import zio.test._
import zio.test.Assertion._

object AccountDepositWithdrawalSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("AccountDepositWithdrawalSpec")(
      testM("Deposit money into Account several times") {
        val key = AccountName("key")

        val moneys: List[Money] = List(Money.usd(123.12), Money.usd(456.45), Money.krw(12519), Money.krw(56947))

        val expectedBalance: MoneyBag = moneys.foldLeft(MoneyBag.empty)(_ + _)

        for {
          (accountEntity, probe) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          account = accountEntity(key)
          _ <- account.initialize(TransactionCost.zero)

          _ <- account.deposit(moneys(0))
          _ <- account.deposit(moneys(1))
          _ <- account.deposit(moneys(2))
          _ <- account.deposit(moneys(3))
          events <- probe.probeForKey(key).events
          balance <- account.balance
          netValue <- account.netValue
        } yield {
          assert(events)(
            equalTo(AccountEvent.Initialize(TransactionCost.zero) :: moneys.map[AccountEvent](AccountEvent.Deposit))
          ) &&
          assert(balance)(equalTo(expectedBalance)) &&
          assert(netValue)(equalTo(expectedBalance))
        }
      },
      testM("Withdraw money from account several times") {
        val key = AccountName("key")

        val expectedBalance = MoneyBag(Map(Currency.USD -> Money.usd(420.43), Currency.KRW -> Money.krw(298096)))

        for {
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
          _ <- account.deposit(Money.krw(1000000))

          _ <- account.withdraw(Money.usd(123.12))
          _ <- account.withdraw(Money.usd(456.45))
          _ <- account.withdraw(Money.krw(232579))
          _ <- account.withdraw(Money.krw(469325))
          balance <- account.balance
          netValue <- account.netValue
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          assert(netValue)(equalTo(expectedBalance))
        }
      },
      testM("Cannot withdraw money when there is not enough balance") {
        val key = AccountName("key")
        for {
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          account = accountEntity(key)
          _ <- account.initialize(TransactionCost.zero)

          _ <- account.deposit(Money.usd(100))
          failure <- account.withdraw(Money.usd(123.12)).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientBalance("Withdrawal failed"))))
        }
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
