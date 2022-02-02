package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, TickerSymbol}
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core._
import zio.entity.test.TestEntityRuntime._
import zio.entity.test.TestMemoryStores
import zio.test.Assertion._
import zio.test._

import java.time.Instant

object AccountBuySellStockSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("AccountBuySellStockSpec")(
      testM("Buy stock") {
        val expectedBalance: MoneyBag =
          MoneyBag(Map(Currency.USD -> BigDecimal(144.5), Currency.KRW -> BigDecimal(163100)))
        val expectedHoldings: Map[TickerSymbol, Holding] =
          Map("AAPL" -> Holding("AAPL", Money.usd(171.1), 5), "005930" -> Holding("005930", Money.krw(69742), 12))

        (for {
          (accountEntity, _) <- testEntityWithProbe[
            String,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          _ <- accountEntity("key").deposit(Money.krw(1000000))
          _ <- accountEntity("key").deposit(Money.usd(1000))

          _ <- accountEntity("key").buy("AAPL", Money.usd(167.2), 2, Instant.now())
          _ <- accountEntity("key").buy("AAPL", Money.usd(173.7), 3, Instant.now())
          _ <- accountEntity("key").buy("005930", Money.krw(71200), 5, Instant.now()) // Samsung Electronics
          _ <- accountEntity("key").buy("005930", Money.krw(68700), 7, Instant.now()) // Samsung Electronics
          balance <- accountEntity("key").balance
          holdings <- accountEntity("key").holdings
        } yield {
          assert(balance)(equalTo(expectedBalance)) &&
          assert(holdings)(equalTo(expectedHoldings))
        }).provideSomeLayer[Environment](layer)
      }
    )

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
