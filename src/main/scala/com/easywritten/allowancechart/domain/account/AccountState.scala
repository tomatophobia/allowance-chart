package com.easywritten.allowancechart.domain.account

import cats.implicits._
import com.easywritten.allowancechart.domain.{Holding, MoneyBag, Stock, Ticker, TransactionCost}
import zio.entity.core.Fold.impossible
import zio._

sealed trait AccountState {
  def handleEvent(e: AccountEvent): Task[AccountState]
}

object AccountState {
  val init: AccountState = PartialAccountState
}

case object PartialAccountState extends AccountState {
  override def handleEvent(e: AccountEvent): Task[AccountState] = e match {
    case AccountEvent.Initialize(cost) =>
      Task.succeed(FullAccountState(balance = MoneyBag.empty, holdings = Set(), cost = cost))
    case _ => impossible
  }
}

final case class FullAccountState(
    balance: MoneyBag,
    holdings: Set[Holding],
    cost: TransactionCost
) extends AccountState {
  def getQuantityByStock(stock: Stock): Int =
    holdings.find(_.stock === stock).map(_.quantity).getOrElse(0)

  def netValue: MoneyBag = holdings.foldLeft(balance) { case (acc, holding) =>
    acc + holding.unitPrice * holding.quantity
  }

  override def handleEvent(e: AccountEvent): Task[AccountState] = e match {
    case AccountEvent.Deposit(money)    => Task.succeed(copy(balance = balance + money))
    case AccountEvent.Withdrawal(money) => Task.succeed(copy(balance = balance - money))
    case AccountEvent.Buy(stock, averagePrice, quantity, _) =>
      val totalAmount = averagePrice * quantity
      val nextHoldings = holdings.find(_.stock === stock) match {
        case Some(h) =>
          val nextQuantity = h.quantity + quantity
          val nextAveragePrice = ((h.unitPrice * h.quantity) unsafe_+ totalAmount) / nextQuantity
          val nextHolding = h.copy(unitPrice = nextAveragePrice, quantity = nextQuantity)
          holdings map {
            case h if h.stock === stock => nextHolding
            case h => h
          }

        case None =>
          val nextHolding = Holding(stock, averagePrice, quantity)
          holdings + nextHolding
      }

      Task.succeed(
        copy(
          balance = balance - totalAmount - totalAmount * cost.buy,
          holdings = nextHoldings
        )
      )
    case AccountEvent.Sell(stock, contractPrice, quantity, _) =>
      val totalAmount = contractPrice * quantity
      for {
        nextHolding <- holdings.find(_.stock === stock) match {
          case Some(h) => Task.succeed(h.copy(quantity = h.quantity - quantity))
          case _       => impossible
        }
      } yield copy(
        balance = balance + totalAmount - totalAmount * cost.sell,
        holdings = holdings map {
          case h if h.stock === stock => nextHolding
          case h => h
        }
      )
    case _ => impossible
  }
}
