package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Money, MoneyBag}
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
          // assertTrue로 바꾸면 비교 못하고 런타임 에러 발생 둘이 뭔가 차이가 있는 듯
        }).provideSomeLayer[Environment](layer)
      }
    )
}
