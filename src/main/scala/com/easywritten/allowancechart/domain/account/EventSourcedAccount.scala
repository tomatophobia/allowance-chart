package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Money, MoneyBag}
import zio._
import zio.entity.core.{Combinators, Fold}
import zio.entity.core.Fold.impossible
import zio.entity.data.Tagging.Const
import zio.entity.data.{EntityProtocol, EventTag, Tagging}
import zio.entity.macros.RpcMacro

class EventSourcedAccount(combinators: Combinators[AccountState, AccountEvent, AccountCommandReject]) extends Account {
  import combinators._

  override def balance: IO[AccountCommandReject, MoneyBag] = read.map(_.balance)

  override def deposit(money: Money): IO[AccountCommandReject, Unit] = read flatMap { state =>
    append(AccountEvent.Deposit(money)).unit
  }

  override def withdraw(money: Money): IO[AccountCommandReject, Unit] = read flatMap { state =>
    if (state.balance.canAfford(MoneyBag.fromMoneys(money)))
      append(AccountEvent.Withdrawal(money)).unit
    else reject(AccountCommandReject.InsufficientBalance)
  }
}

object EventSourcedAccount {
  val tagging: Const[String] = Tagging.const[String](EventTag("Account"))

  val eventHandlerLogic: Fold[AccountState, AccountEvent] = Fold(
    initial = AccountState.init,
    reduce = {
      case (state, AccountEvent.Deposit(money))    => UIO.succeed(state.copy(balance = state.balance + money))
      case (state, AccountEvent.Withdrawal(money)) => UIO.succeed(state.copy(balance = state.balance - money))
      case _                                       => impossible
    }
  )

  import AccountCommandReject.accountCommandRejectPickler

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountProtocol: EntityProtocol[Account, AccountCommandReject] =
    RpcMacro.derive[Account, AccountCommandReject]
}
