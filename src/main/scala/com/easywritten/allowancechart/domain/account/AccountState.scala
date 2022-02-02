package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, MoneyBag, TickerSymbol}

final case class AccountState(
    balance: MoneyBag,
    holdings: Map[TickerSymbol, Holding]
)

object AccountState {
  val init: AccountState = AccountState(balance = MoneyBag.empty, holdings = Map())
}
