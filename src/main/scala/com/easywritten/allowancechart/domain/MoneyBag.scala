package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, MoneyAmount]) {
  def +(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.getOrElse(money.currency, ZeroAmount) + money.amount
    copy(moneys.updated(money.currency, newAmount))
  }
}
