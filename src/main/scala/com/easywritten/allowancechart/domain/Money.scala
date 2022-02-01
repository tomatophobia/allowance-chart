package com.easywritten.allowancechart.domain

final case class Money(currency: Currency, amount: BigDecimal) {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def +(other: Money): Money = {
    if (currency != other.currency) throw new IllegalArgumentException
    else Money(currency, amount + other.amount)
  }
}

object Money {
  def usd(amount: BigDecimal): Money = Money(USD, amount)
  def krw(amount: BigDecimal): Money = Money(KRW, amount)
}

sealed trait Currency
case object USD extends Currency
case object KRW extends Currency
