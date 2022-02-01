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
  def jpy(amount: MoneyAmount): Money = Money(Currency.JPY, amount)
  def cny(amount: MoneyAmount): Money = Money(Currency.CNY, amount)
  def hkd(amount: MoneyAmount): Money = Money(Currency.HKD, amount)
  def eur(amount: MoneyAmount): Money = Money(Currency.EUR, amount)
  def sgd(amount: MoneyAmount): Money = Money(Currency.SGD, amount)
  def cad(amount: MoneyAmount): Money = Money(Currency.CAD, amount)
  def chf(amount: MoneyAmount): Money = Money(Currency.CHF, amount)
  def aud(amount: MoneyAmount): Money = Money(Currency.AUD, amount)
  def gbp(amount: MoneyAmount): Money = Money(Currency.GBP, amount)

  implicit val eqMoney: Eq[Money] = Eq.fromUniversalEquals
}

sealed trait Currency extends EnumEntry

object Currency extends Enum[Currency] {
  case object USD extends Currency
  case object KRW extends Currency
  case object JPY extends Currency
  case object CNY extends Currency
  case object HKD extends Currency
  case object EUR extends Currency
  case object SGD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object AUD extends Currency
  case object GBP extends Currency

  override def values: IndexedSeq[Currency] = findValues

  implicit val eqCurrency: Eq[Currency] = Eq.fromUniversalEquals
}
