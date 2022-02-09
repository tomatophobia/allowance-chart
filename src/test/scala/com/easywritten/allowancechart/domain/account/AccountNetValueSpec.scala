package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain._
import zio._
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core._
import zio.entity.test.TestEntityRuntime._
import zio.entity.test.TestMemoryStores
import zio.test.Assertion._
import zio.test._

object AccountNetValueSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = {
    suite("AccountNetValueSpec")(
      // TODO 총 평가액의 변화가 어떻게 될지 테스트에 반영 (아마 다음 이슈에서..?)
      testM("Net value slightly increase after Buy some stocks") {
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> BigDecimal(144.5), Currency.KRW -> BigDecimal(163100)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(171.1), 5), "005930" -> Holding("005930", Money.krw(69742), 12))
        val expectedNetValue: MoneyBag =
          MoneyBag(Map(Currency.USD -> BigDecimal(1000000), Currency.KRW -> BigDecimal(1000)))

        (for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.krw(1000000))
          _ <- accountEntity("key").deposit(Money.usd(1000))

          _ <- accountEntity("key").buy("AAPL", Money.usd(167.2), 2, now)
          _ <- accountEntity("key").buy("AAPL", Money.usd(173.7), 3, now)
          _ <- accountEntity("key").buy("005930", Money.krw(71200), 5, now) // Samsung Electronics
          _ <- accountEntity("key").buy("005930", Money.krw(68700), 7, now) // Samsung Electronics
          balance <- accountEntity("key").balance
          holdings <- accountEntity("key").holdings
          netValue <- accountEntity("key").netValue
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          assert(holdings)(equalTo(expectedHoldings)) &&
          assert(netValue)(equalTo(expectedNetValue))
        }).provideSomeLayer[Environment](layer)
      } @@ TestAspect.ignore
    )
  }

  import EventSourcedAccount.accountProtocol

  private val layer = Clock.any ++ TestMemoryStores.make[String, AccountEvent, AccountState](50.millis) >>>
    testEntity(
      EventSourcedAccount.tagging,
      EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountCommandReject](
        new EventSourcedAccount(_),
        EventSourcedAccount.eventHandlerLogic,
        AccountCommandReject.FromThrowable
      )
    )
}
