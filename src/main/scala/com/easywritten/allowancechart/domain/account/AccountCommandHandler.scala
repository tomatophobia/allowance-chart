package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, SecuritiesCompany, Stock}
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

class AccountCommandHandler(combinators: Combinators[AccountState, AccountEvent, AccountError]) extends Account {
  import combinators._

  override def initialize(company: SecuritiesCompany): IO[AccountError, Unit] = read flatMap { _ =>
    reject(AccountError.AccountAlreadyInitialized)
//    case IdleAccountState => append(AccountEvent.Initialize(company))
//    case _                => reject(AccountError.AccountAlreadyInitialized)
  }

  override def balance: IO[AccountError, MoneyBag] = ensureFullState map (_.balance)

  override def holdings: IO[AccountError, Set[Holding]] = ensureFullState map (_.holdings)

  override def netValue: IO[AccountError, MoneyBag] = ensureFullState map (_.netValue)

  override def deposit(money: Money, at: Instant): IO[AccountError, Unit] = ensureFullState flatMap { _ =>
    append(AccountEvent.Deposit(money, at))
  }

  override def withdraw(money: Money, at: Instant): IO[AccountError, Unit] = ensureFullState flatMap { state =>
    if (state.balance.canAfford(MoneyBag.fromMoneys(money)))
      append(AccountEvent.Withdrawal(money, at))
    else reject(AccountError.InsufficientBalance("Withdrawal failed"))
  }

  override def buy(
      stock: Stock,
      unitPrice: Money,
      quantity: Int,
      at: Instant
  ): IO[AccountError, Unit] =
    ensureFullState flatMap { state =>
      if (state.balance.canAfford(MoneyBag.fromMoneys(unitPrice * quantity)))
        append(AccountEvent.Buy(stock, unitPrice, quantity, at))
      else reject(AccountError.InsufficientBalance("Buying failed"))
    }

  override def sell(
      stock: Stock,
      contractPrice: Money,
      quantity: Int,
      at: Instant
  ): IO[AccountError, Unit] =
    ensureFullState flatMap { state =>
      if (state.getQuantityByStock(stock) >= quantity)
        append(AccountEvent.Sell(stock, contractPrice, quantity, at))
      else reject(AccountError.InsufficientShares("Selling failed"))
    }

  override def dividendPaid(stock: Stock, amount: Money, tax: Money, at: Instant): IO[AccountError, Unit] =
    ensureFullState flatMap (_ => append(AccountEvent.DividendPaid(stock, amount, tax, at)))

  override def foreignExchangeBuy(
      exchange: MoneyBag,
      exchangeRate: BigDecimal,
      at: Instant
  ): IO[AccountError, Unit] =
    ensureFullState flatMap (_ => append(AccountEvent.ForeignExchangeBuy(exchange, exchangeRate, at)))

  private def ensureFullState: IO[AccountError, ActiveAccountState] =
    read flatMap {
      case state: ActiveAccountState => IO.succeed(state)
      case IdleAccountState          => reject(AccountError.AccountNotInitialized)
    }
}

object AccountCommandHandler {
  val tagging: Const[AccountName] = Tagging.const[AccountName](EventTag("Account"))

  val eventHandlerLogic: Fold[AccountState, AccountEvent] = Fold(initial = AccountState.init, reduce = _.handleEvent(_))

  import AccountError.accountErrorPickler

  implicit val instantPickler: Pickler[Instant] =
    boopickle.DefaultBasic.longPickler.xmap(Instant.ofEpochMilli)(_.toEpochMilli)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountProtocol: EntityProtocol[Account, AccountError] =
    RpcMacro.derive[Account, AccountError]

  // TODO in-memory말고 postgres로 변경
  private val stores: ZLayer[Any, Nothing, Has[Stores[AccountName, AccountEvent, AccountState]]] =
    Clock.live to MemoryStores.make[AccountName, AccountEvent, AccountState](100.millis, 2)

  implicit val accountNameStringEncoder: StringEncoder[AccountName] = s => s.name
  implicit val accountNameStringDecoder: StringDecoder[AccountName] = n => Some(AccountName(n))

  val accounts: RLayer[ZEnv, Has[Entity[AccountName, Account, AccountState, AccountEvent, AccountError]]] =
    (Clock.live and stores and Runtime.actorSettings("AccountActorSystem")) to Runtime
      .entityLive(
        "Account",
        tagging,
        EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountError](
          new AccountCommandHandler(_),
          eventHandlerLogic,
          AccountError.FromThrowable
        )
      )
      .toLayer
}
