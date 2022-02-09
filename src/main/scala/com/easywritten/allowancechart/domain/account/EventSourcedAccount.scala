package com.easywritten.allowancechart.domain.account

import boopickle.{CompositePickler, Pickler}
import boopickle.Default.generatePickler
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, TickerSymbol}
import zio._
import zio.entity.core.{Combinators, Fold}
import zio.entity.core.Fold.impossible
import zio.entity.data.Tagging.Const
import zio.entity.data.{EntityProtocol, EventTag, Tagging}
import zio.entity.macros.RpcMacro

import java.time.Instant

class EventSourcedAccount(combinators: Combinators[AccountState, AccountEvent, AccountCommandReject]) extends Account {
  import combinators._

  override def balance: IO[AccountCommandReject, MoneyBag] = read map (_.balance)

  override def holdings: IO[AccountCommandReject, Map[TickerSymbol, Holding]] = read map (_.holdings)

  override def netValue: IO[AccountCommandReject, MoneyBag] = read map (_.netValue)

  override def deposit(money: Money): IO[AccountCommandReject, Unit] = read flatMap { _ =>
    append(AccountEvent.Deposit(money)).unit
  }

  override def withdraw(money: Money): IO[AccountCommandReject, Unit] = read flatMap { state =>
    if (state.balance.canAfford(MoneyBag.fromMoneys(money)))
      append(AccountEvent.Withdrawal(money)).unit
    else reject(AccountCommandReject.InsufficientBalance("Withdrawal failed"))
  }

  override def buy(
      symbol: TickerSymbol,
      averagePrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    read flatMap { state =>
      if (state.balance.canAfford(MoneyBag.fromMoneys(averagePrice * quantity)))
        append(AccountEvent.Buy(symbol, averagePrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientBalance("Buying failed"))
    }

  override def sell(
      symbol: TickerSymbol,
      contractPrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    read flatMap { state =>
      if (state.getQuantityBySymbol(symbol) >= quantity)
        append(AccountEvent.Sell(symbol, contractPrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientShares("Selling failed"))
    }
}

object EventSourcedAccount {
  val tagging: Const[String] = Tagging.const[String](EventTag("Account"))

  val eventHandlerLogic: Fold[AccountState, AccountEvent] = Fold(
    initial = AccountState.init,
    // TODO reduce 로직을 AccountState로 옮기기?
    reduce = {
      case (state, AccountEvent.Deposit(money))    => UIO.succeed(state.copy(balance = state.balance + money))
      case (state, AccountEvent.Withdrawal(money)) => UIO.succeed(state.copy(balance = state.balance - money))
      case (state, AccountEvent.Buy(symbol, averagePrice, quantity, _)) =>
        val nextHolding = state.holdings.get(symbol) match {
          case Some(h) =>
            val nextQuantity = h.quantity + quantity
            // 올림함으로써 평단가를 높게 잡아 수익률이 보수적으로 측정되도록 함, 대신 매수 후 총 평가액도 그만큼 오차가 생김
            val nextAveragePrice =
              (((h.averagePrice * h.quantity) unsafe_+ (averagePrice * quantity)) / nextQuantity).ceiling
            h.copy(quantity = nextQuantity, averagePrice = nextAveragePrice)
          case None => Holding(symbol, averagePrice, quantity)
        }
        UIO.succeed(
          state.copy(
            balance = state.balance - averagePrice * quantity,
            holdings = state.holdings.updated(symbol, nextHolding)
          )
        )
      case (state, AccountEvent.Sell(symbol, contractPrice, quantity, _)) =>
        for {
          nextHolding <- state.holdings.get(symbol) match {
            case Some(h) => UIO.succeed(h.copy(quantity = h.quantity - quantity))
            case _       => impossible
          }
        } yield state.copy(
          balance = state.balance + contractPrice * quantity,
          holdings = state.holdings.updated(symbol, nextHolding)
        )
      case _ => impossible
    }
  )

  import AccountCommandReject.accountCommandRejectPickler

  implicit val instantPickler: Pickler[Instant] =
    boopickle.DefaultBasic.longPickler.xmap(Instant.ofEpochMilli)(_.toEpochMilli)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountProtocol: EntityProtocol[Account, AccountCommandReject] =
    RpcMacro.derive[Account, AccountCommandReject]
}
