package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, MoneyBag, TickerSymbol, TransactionCost}
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
      Task.succeed(FullAccountState(balance = MoneyBag.empty, holdings = Map(), cost = cost))
    case _ => impossible
  }
}

final case class FullAccountState(
    balance: MoneyBag,
    holdings: Map[TickerSymbol, Holding],
    cost: TransactionCost
) extends AccountState {
  def getQuantityBySymbol(symbol: TickerSymbol): Int =
    holdings(symbol).quantity

  def netValue: MoneyBag = holdings.foldLeft(balance) { case (acc, (_, holding)) =>
    acc + holding.averagePrice * holding.quantity
  }

  override def handleEvent(e: AccountEvent): Task[AccountState] = e match {
    case AccountEvent.Deposit(money)    => Task.succeed(copy(balance = balance + money))
    case AccountEvent.Withdrawal(money) => Task.succeed(copy(balance = balance - money))
    case AccountEvent.Buy(symbol, averagePrice, quantity, _) =>
      val nextHolding = holdings.get(symbol) match {
        case Some(h) =>
          val nextQuantity = h.quantity + quantity
          val nextAveragePrice = ((h.averagePrice * h.quantity) unsafe_+ (averagePrice * quantity)) / nextQuantity
          h.copy(quantity = nextQuantity, averagePrice = nextAveragePrice)
        case None => Holding(symbol, averagePrice, quantity)
      }
      Task.succeed(
        copy(
          balance = balance - averagePrice * quantity,
          holdings = holdings.updated(symbol, nextHolding)
        )
      )
    case AccountEvent.Sell(symbol, contractPrice, quantity, _) =>
      for {
        nextHolding <- holdings.get(symbol) match {
          case Some(h) => Task.succeed(h.copy(quantity = h.quantity - quantity))
          case _       => impossible
        }
      } yield copy(
        balance = balance + contractPrice * quantity,
        holdings = holdings.updated(symbol, nextHolding)
      )
    case _ => impossible
  }
}
