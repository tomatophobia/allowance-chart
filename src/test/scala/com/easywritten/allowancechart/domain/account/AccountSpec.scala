package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Currency, Money, MoneyBag}
import zio._
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core._
import zio.entity.readside.ReadSideParams
import zio.entity.test.TestEntityRuntime._
import zio.entity.test.TestMemoryStores
import zio.test._
import zio.test.Assertion._

object AccountSpec extends DefaultRunnableSpec {

  import EventSourcedAccount.accountProtocol

  private val layer = Clock.any ++ TestMemoryStores.make[String, AccountEvent, AccountState](50.millis) >>>
    testEntity(
      EventSourcedAccount.tagging,
      EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountCommandReject](
        new EventSourcedAccount(_),
        EventSourcedAccount.eventHandlerLogic,
        e => {
          AccountCommandReject.FromThrowable(Option(e))
        }
      )
    )

  override def spec: ZSpec[Environment, Failure] =
    suite("AccountSpec")(
      testM("Deposit money into Account several times") {
        val moneys: List[Money] = List(Money.usd(123.12), Money.usd(456.45), Money.krw(12519), Money.krw(56947))

        val expectedBalance: MoneyBag = moneys.foldLeft(MoneyBag.empty)(_ + _)

        (for {
          (accountEntity, probe) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(moneys(0))
          _ <- accountEntity("key").deposit(moneys(1))
          _ <- accountEntity("key").deposit(moneys(2))
          _ <- accountEntity("key").deposit(moneys(3))
          events <- probe.probeForKey("key").events
          balance <- accountEntity("key").balance
        } yield {
          assert(events)(equalTo(moneys.map(AccountEvent.Deposit))) &&
          assert(balance)(equalTo(expectedBalance))
        }).provideSomeLayer[Environment](layer)
      },
      testM("Withdraw money from account several times") {
        val expectedBalance = MoneyBag(Map(Currency.USD -> BigDecimal(420.43), Currency.KRW -> BigDecimal(298096)))

        (for {
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.usd(1000))
          _ <- accountEntity("key").deposit(Money.krw(1000000))

          _ <- accountEntity("key").withdraw(Money.usd(123.12))
          _ <- accountEntity("key").withdraw(Money.usd(456.45))
          _ <- accountEntity("key").withdraw(Money.krw(232579))
          _ <- accountEntity("key").withdraw(Money.krw(469325))
          balance <- accountEntity("key").balance
        } yield {
          assert(balance)(equalTo(expectedBalance))
        }).provideSomeLayer[Environment](layer)
      },
      testM("Cannot withdraw money when there is not enough balance") {
        (for {
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.usd(100))
          failure <- accountEntity("key").withdraw(Money.usd(123.12)).run
        } yield {
          assert(failure)(fails(equalTo(AccountCommandReject.InsufficientBalance)))
        }).provideSomeLayer[Environment](layer)
      },
    )
}
