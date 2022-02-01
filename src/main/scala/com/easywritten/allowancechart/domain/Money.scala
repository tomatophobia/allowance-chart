package com.easywritten.allowancechart.domain

import cats.kernel.Eq
import cats.implicits._
import enumeratum._

final case class Money(currency: Currency, amount: MoneyAmount) {

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafe_+(other: Money): Money = {
    if (currency === other.currency)
      Money(currency, amount + other.amount)
    else
      throw new IllegalArgumentException(
        s"${currency.entryName} and ${other.currency.entryName} are not compatible"
      )
  }

}

object Money {
  def usd(amount: MoneyAmount): Money = Money(Currency.USD, amount)
  def krw(amount: MoneyAmount): Money = Money(Currency.KRW, amount)

  implicit val eqMoney: Eq[Money] = Eq.fromUniversalEquals
}

sealed trait Currency extends EnumEntry

object Currency extends Enum[Currency] {
  case object USD extends Currency
  case object KRW extends Currency

  override def values: IndexedSeq[Currency] = findValues

  implicit val eqCurrency: Eq[Currency] = Eq.fromUniversalEquals
}