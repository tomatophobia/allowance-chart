package com.easywritten.allowancechart.application.port.in

import java.time.LocalDate
import cats.kernel.Eq
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Stock}
import enumeratum._

sealed abstract class TransactionRecord(date: LocalDate, transactionClass: String, briefName: String)
    extends EnumEntry
    with Product
    with Serializable

object TransactionRecord extends Enum[TransactionRecord] {

  final case class Deposit(date: LocalDate, transactionClass: String, amount: Money, briefName: String)
      extends TransactionRecord(date, transactionClass, briefName)

  final case class Dividend(
      date: LocalDate,
      transactionClass: String,
      amount: Money,
      stock: Stock,
      briefName: String,
      tax: Money
  ) extends TransactionRecord(date, transactionClass, briefName)

  final case class ForeignExchangeBuy(
      date: LocalDate,
      transactionClass: String,
      fx: MoneyBag,
      exchangeRate: BigDecimal,
      briefName: String
  ) extends TransactionRecord(date, transactionClass, briefName)

  final case class Buy(
      date: LocalDate,
      transactionClass: String,
      totalPrice: Money,
      holding: Holding,
      briefName: String,
      fee: Money
  ) extends TransactionRecord(date, transactionClass, briefName)

  final case class Sell(
      date: LocalDate,
      transactionClass: String,
      totalPrice: Money,
      holding: Holding,
      briefName: String,
      fee: Money,
      tax: Money
  ) extends TransactionRecord(date, transactionClass, briefName)

  final override def values: IndexedSeq[TransactionRecord] = findValues

  implicit val eqTransactionRecord: Eq[TransactionRecord] = Eq.fromUniversalEquals
}
