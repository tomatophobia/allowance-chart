package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Stock, TransactionCost}
import zio._
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core.{
  Combinators,
  Entity,
  EventSourcedBehaviour,
  Fold,
  MemoryStores,
  Stores,
  StringEncoder,
  StringDecoder
}
import zio.entity.data.Tagging.Const
import zio.entity.data.{EntityProtocol, EventTag, Tagging}
import zio.entity.macros.RpcMacro
import zio.entity.runtime.akka.Runtime

import java.time.Instant

class EventSourcedAccount(combinators: Combinators[AccountState, AccountEvent, AccountCommandReject]) extends Account {
  import combinators._

  override def initialize(cost: TransactionCost): IO[AccountCommandReject, Unit] = read flatMap {
    case IdleAccountState => append(AccountEvent.Initialize(cost))
    case _                => reject(AccountCommandReject.AccountAlreadyInitialized)
  }

  override def balance: IO[AccountCommandReject, MoneyBag] = ensureFullState map (_.balance)

  override def holdings: IO[AccountCommandReject, Set[Holding]] = ensureFullState map (_.holdings)

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
      stock: Stock,
      averagePrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    ensureFullState flatMap { state =>
      if (state.balance.canAfford(MoneyBag.fromMoneys(averagePrice * quantity)))
        append(AccountEvent.Buy(stock, averagePrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientBalance("Buying failed"))
    }

  override def sell(
      stock: Stock,
      contractPrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit] =
    ensureFullState flatMap { state =>
      if (state.getQuantityByStock(stock) >= quantity)
        append(AccountEvent.Sell(stock, contractPrice, quantity, contractedAt))
      else reject(AccountCommandReject.InsufficientShares("Selling failed"))
    }

  private def ensureFullState: IO[AccountCommandReject, ActiveAccountState] =
    read flatMap {
      case state: ActiveAccountState => IO.succeed(state)
      case IdleAccountState          => reject(AccountCommandReject.AccountNotInitialized)
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

  // TODO in-memory말고 postgres로 변경
  private val stores: ZLayer[Any, Nothing, Has[Stores[AccountName, AccountEvent, AccountState]]] =
    Clock.live to MemoryStores.make[AccountName, AccountEvent, AccountState](100.millis, 2)

  implicit val accountNameStringEncoder: StringEncoder[AccountName] = s => s.name
  implicit val accountNameStringDecoder: StringDecoder[AccountName] = n => Some(AccountName(n))

  val accounts: RLayer[ZEnv, Has[Entity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject]]] =
    (Clock.live and stores and Runtime.actorSettings("Test")) to Runtime
      .entityLive(
        "Counter",
        tagging,
        EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountCommandReject](
          new EventSourcedAccount(_),
          eventHandlerLogic,
          AccountCommandReject.FromThrowable
        )
      )
      .toLayer
}
