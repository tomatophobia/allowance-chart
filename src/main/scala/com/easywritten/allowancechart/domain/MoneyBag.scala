package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, MoneyAmount]) {
  def +(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.getOrElse(money.currency, ZeroAmount) + money.amount
    copy(moneys.updated(money.currency, newAmount))
  }
}

object MoneyBag {
  val empty: MoneyBag = MoneyBag(Map())

  def fromMoneys(moneys: Money*): MoneyBag =
    MoneyBag(moneys.map(m => m.currency -> m.amount).toMap)
}
