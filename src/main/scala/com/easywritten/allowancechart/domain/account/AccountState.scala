package com.easywritten.allowancechart.domain.account

import cats.implicits._
import com.easywritten.allowancechart.domain.{Holding, MoneyBag, SecuritiesCompany, Stock, Ticker, TransactionCost}
import zio.entity.core.Fold.impossible
import zio._

sealed trait AccountState {
  def handleEvent(e: AccountEvent): Task[AccountState]
}

object AccountState {
  val init: AccountState = IdleAccountState
}

case object IdleAccountState extends AccountState {
  override def handleEvent(e: AccountEvent): Task[AccountState] = e match {
    case AccountEvent.Initialize(company) =>
      Task.succeed(ActiveAccountState(company = company, balance = MoneyBag.empty, holdings = Set()))
    case _ => impossible
  }
}

final case class ActiveAccountState(
    company: SecuritiesCompany,
    balance: MoneyBag,
    holdings: Set[Holding]
) extends AccountState {
  def getQuantityByStock(stock: Stock): Int =
    holdings.find(_.stock === stock).map(_.quantity).getOrElse(0)

  def netValue: MoneyBag = holdings.foldLeft(balance) { case (acc, holding) =>
    acc.add(holding.unitPrice * holding.quantity)
  }

  override def handleEvent(e: AccountEvent): Task[AccountState] = e match {
    case AccountEvent.Deposit(money, _) => Task.succeed(copy(balance = balance.add(money)))

    case AccountEvent.Withdrawal(money, _) => Task.succeed(copy(balance = balance.subtract(money)))

    case AccountEvent.Buy(stock, unitPrice, quantity, _) =>
      val totalAmount = unitPrice * quantity
      val nextHoldings = holdings.find(_.stock === stock) match {
        case Some(h) =>
          val nextQuantity = h.quantity + quantity
          val nextUnitPrice = ((h.unitPrice * h.quantity) unsafe_+ totalAmount) / nextQuantity
          val nextHolding = h.copy(unitPrice = nextUnitPrice, quantity = nextQuantity)
          holdings map {
            case h if h.stock === stock => nextHolding
            case h                      => h
          }

        case None =>
          val nextHolding = Holding(stock, unitPrice, quantity)
          holdings + nextHolding
      }

      Task.succeed(
        copy(
          balance = balance.subtract(totalAmount),
          holdings = nextHoldings
        )
      )

    case AccountEvent.Sell(stock, contractPrice, quantity, _) =>
      val totalAmount = contractPrice * quantity
      val maybeNextHolding = holdings.find(_.stock === stock).map(h => h.copy(quantity = h.quantity - quantity))

      maybeNextHolding match {
        case Some(nextHolding) if nextHolding.quantity > 0 =>
          Task.succeed(
            copy(
              balance = balance.add(totalAmount),
              holdings = holdings collect {
                case h if h.stock === stock => nextHolding
                case h                      => h
              }
            )
          )
        case Some(nextHolding) if nextHolding.quantity === 0 =>
          Task.succeed(
            copy(
              balance = balance.add(totalAmount),
              holdings = holdings.filter(_.stock =!= stock)
            )
          )
        case _ => impossible
      }

    case AccountEvent.DividendPaid(_, amount, tax, _) =>
      // TODO 배당금을 따로 모아서 관리...
      Task.succeed(copy(balance = balance.add(amount).subtract(tax)))

    case AccountEvent.ForeignExchangeBuy(fx, _, _) =>
      Task.succeed(copy(balance = balance + fx))

    case _ => impossible
  }
}
