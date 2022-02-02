package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, MoneyAmount]) {
  def +(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.getOrElse(money.currency, ZeroAmount) + money.amount
    copy(moneys.updated(money.currency, newAmount))
  }

  // MoneyBag이 마이너스가 되는 것을 방지하는 validation은 여기에서 해주어야 하나?
  def -(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.getOrElse(money.currency, ZeroAmount) - money.amount
    copy(moneys.updated(money.currency, newAmount))
  }
}

object MoneyBag {
  val empty: MoneyBag = MoneyBag(Map())

  def fromMoneys(moneys: Money*): MoneyBag =
    MoneyBag(moneys.map(m => m.currency -> m.amount).toMap)
}
