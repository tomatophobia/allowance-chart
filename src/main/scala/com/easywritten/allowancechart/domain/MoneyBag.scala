package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, Money]) {
  def add(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.get(money.currency).map(_.amount).getOrElse(ZeroAmount) + money.amount
    copy(moneys.updated(money.currency, Money(money.currency, newAmount)))
  }

  // MoneyBag이 마이너스가 되는 것을 방지하는 validation은 여기에서 해주어야 하나?
  def subtract(money: Money): MoneyBag = {
    val newAmount: MoneyAmount = moneys.get(money.currency).map(_.amount).getOrElse(ZeroAmount) - money.amount
    copy(moneys.updated(money.currency, Money(money.currency, newAmount)))
  }

  def +(that: MoneyBag): MoneyBag =
    MoneyBag(
      (this.moneys.keySet ++ that.moneys.keySet).iterator.map { currency =>
        val zeroMoney = Money(currency, ZeroAmount)
        val newValue = this.moneys.getOrElse(currency, zeroMoney) unsafe_+ that.moneys.getOrElse(currency, zeroMoney)
        currency -> newValue
      }.toMap
    )

  def -(that: MoneyBag): MoneyBag =
    MoneyBag(
      (this.moneys.keySet ++ that.moneys.keySet).iterator.map { currency =>
        val zeroMoney = Money(currency, ZeroAmount)
        val newValue = this.moneys.getOrElse(currency, zeroMoney) unsafe_- that.moneys.getOrElse(currency, zeroMoney)
        currency -> newValue
      }.toMap
    )

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
