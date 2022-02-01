package com.easywritten.allowancechart.domain

final case class MoneyBag(moneys: Map[Currency, BigDecimal]) {
  def +(money: Money): MoneyBag = {
    val newAmount: BigDecimal = moneys.getOrElse(money.currency, BigDecimal(0)) + money.amount
    copy(moneys.updated(money.currency, newAmount))
  }
}
