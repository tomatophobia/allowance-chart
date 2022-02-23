package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Ticker, TransactionCost}
import zio._
import zio.entity.core.{Combinators, Fold}
import zio.entity.data.Tagging.Const
import zio.entity.data.{EntityProtocol, EventTag, Tagging}
import zio.entity.macros.RpcMacro

import java.time.Instant

class EventSourcedAccount(combinators: Combinators[AccountState, AccountEvent, AccountCommandReject]) extends Account {
  import combinators._

  override def initialize(cost: TransactionCost): IO[AccountCommandReject, Unit] = read flatMap {
    case PartialAccountState => append(AccountEvent.Initialize(cost))
    case _                   => reject(AccountCommandReject.AccountAlreadyInitialized)
  }

  override def balance: IO[AccountCommandReject, MoneyBag] = ensureFullState map (_.balance)

  override def holdings: IO[AccountCommandReject, Map[Ticker, Holding]] = ensureFullState map (_.holdings)

  override def netValue: IO[AccountCommandReject, MoneyBag] = ensureFullState map (_.netValue)

  override def deposit(money: Money): IO[AccountCommandReject, Unit] = ensureFullState flatMap { _ =>
    append(AccountEvent.Deposit(money))
  }

  override def withdraw(money: Money): IO[AccountCommandReject, Unit] = ensureFullState flatMap { state =>
    if (state.balance.canAfford(MoneyBag.fromMoneys(money)))
      append(AccountEvent.Withdrawal(money))
    else reject(AccountCommandReject.InsufficientBalance("Withdrawal failed"))
  }

  override def buy(
      symbol: Ticker,
      averagePrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    ensureFullState flatMap { state =>
      if (state.balance.canAfford(MoneyBag.fromMoneys(averagePrice * quantity)))
        append(AccountEvent.Buy(symbol, averagePrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientBalance("Buying failed"))
    }

  override def sell(
      symbol: Ticker,
      contractPrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    ensureFullState flatMap { state =>
      if (state.getQuantityBySymbol(symbol) >= quantity)
        append(AccountEvent.Sell(symbol, contractPrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientShares("Selling failed"))
    }

  private def ensureFullState: IO[AccountCommandReject, FullAccountState] =
    read flatMap {
      case state: FullAccountState => IO.succeed(state)
      case PartialAccountState     => reject(AccountCommandReject.AccountNotInitialized)
    }
}

object EventSourcedAccount {
  val tagging: Const[AccountName] = Tagging.const[AccountName](EventTag("Account"))

  val eventHandlerLogic: Fold[AccountState, AccountEvent] = Fold(initial = AccountState.init, reduce = _.handleEvent(_))

  import AccountCommandReject.accountCommandRejectPickler

  implicit val instantPickler: Pickler[Instant] =
    boopickle.DefaultBasic.longPickler.xmap(Instant.ofEpochMilli)(_.toEpochMilli)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountProtocol: EntityProtocol[Account, AccountCommandReject] =
    RpcMacro.derive[Account, AccountCommandReject]
}
