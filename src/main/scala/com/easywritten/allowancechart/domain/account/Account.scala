package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, TickerSymbol}
import zio._
import zio.entity.annotations.Id

import java.time.Instant

trait Account {
  @Id(1)
  def balance: IO[AccountCommandReject, MoneyBag]

  @Id(2)
  def holdings: IO[AccountCommandReject, Map[TickerSymbol, Holding]]

  @Id(3)
  def deposit(money: Money): IO[AccountCommandReject, Unit]

  @Id(4)
  def withdraw(money: Money): IO[AccountCommandReject, Unit]

  @Id(5)
  def buy(
      symbol: TickerSymbol,
      averagePrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit]

  @Id(6)
  def sell(
      symbol: TickerSymbol,
      contractPrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit]

}
