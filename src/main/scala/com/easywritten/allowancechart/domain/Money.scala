package com.easywritten.allowancechart.domain

import cats.kernel.Eq
import cats.implicits._
import enumeratum._

import scala.math.BigDecimal.RoundingMode

final case class Money(currency: Currency, amount: MoneyAmount) {

  def floor: Money = copy(amount = amount.setScale(currency.scale, RoundingMode.FLOOR))

  def ceiling: Money = copy(amount = amount.setScale(currency.scale, RoundingMode.CEILING))

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafe_+(other: Money): Money = {
    if (currency === other.currency)
      Money(currency, amount + other.amount)
    else
      throw new IllegalArgumentException(
        s"${currency.entryName} and ${other.currency.entryName} are not compatible"
      )
  }

  def *(i: Int): Money = copy(amount = amount * i)

  // TODO currency 따라서 scaling
  def /(i: Int): Money = copy(amount = amount / i)

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

sealed abstract class Currency(val scale: Int) extends EnumEntry with Product with Serializable

object Currency extends Enum[Currency] {
  case object USD extends Currency(2)
  case object KRW extends Currency(0)
  case object JPY extends Currency(0)
  case object CNY extends Currency(0)
  case object HKD extends Currency(0)
  case object EUR extends Currency(0)
  case object SGD extends Currency(0)
  case object CAD extends Currency(0)
  case object CHF extends Currency(0)
  case object AUD extends Currency(0)
  case object GBP extends Currency(0)

  override def values: IndexedSeq[Currency] = findValues

  implicit val eqCurrency: Eq[Currency] = Eq.fromUniversalEquals
}
