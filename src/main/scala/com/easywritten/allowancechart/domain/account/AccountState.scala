package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, MoneyBag, TickerSymbol}

final case class AccountState(
    balance: MoneyBag,
    holdings: Map[TickerSymbol, Holding]
) {
  def getQuantityBySymbol(symbol: TickerSymbol): Int =
    holdings(symbol).quantity

  def netValue: MoneyBag = holdings.foldLeft(balance) {
    case (acc, (_, holding)) => acc + holding.averagePrice * holding.quantity
  }
}

object AccountState {
  val init: AccountState = AccountState(balance = MoneyBag.empty, holdings = Map())
}
