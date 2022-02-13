package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, Money]) {
  def +(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.get(money.currency).map(_.amount).getOrElse(ZeroAmount) + money.amount
    copy(moneys.updated(money.currency, Money(money.currency, newAmount)))
  }

  // MoneyBag이 마이너스가 되는 것을 방지하는 validation은 여기에서 해주어야 하나?
  def -(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.get(money.currency).map(_.amount).getOrElse(ZeroAmount) - money.amount
    copy(moneys.updated(money.currency, Money(money.currency, newAmount)))
  }

  def canAfford(that: MoneyBag): Boolean =
    that.moneys.forall { case (currency, thatMoney) =>
      this.moneys.get(currency).map(_.amount).getOrElse(ZeroAmount) >= thatMoney.amount
    }

  def halfUpAll: MoneyBag = copy(moneys.map { case (currency, money) => currency -> money.halfUp })
}

object MoneyBag {
  val empty: MoneyBag = MoneyBag(Map())

  def fromMoneys(moneys: Money*): MoneyBag =
    MoneyBag(moneys.map(m => m.currency -> m).toMap)
}
