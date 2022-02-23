package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Ticker, TransactionCost}
import zio._
import zio.entity.annotations.Id

import java.time.Instant

trait Account {
  @Id(1)
  def initialize(cost: TransactionCost): IO[AccountCommandReject, Unit]

  @Id(2)
  def balance: IO[AccountCommandReject, MoneyBag]

  @Id(3)
  def holdings: IO[AccountCommandReject, Map[Ticker, Holding]]

  @Id(4)
  def netValue: IO[AccountCommandReject, MoneyBag]

  @Id(5)
  def deposit(money: Money): IO[AccountCommandReject, Unit]

  @Id(6)
  def withdraw(money: Money): IO[AccountCommandReject, Unit]

  @Id(7)
  def buy(
      symbol: Ticker,
      averagePrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit]

  @Id(8)
  def sell(
      symbol: Ticker,
      contractPrice: Money,
      quantity: Int,
      contractedAt: Instant
  ): IO[AccountCommandReject, Unit]

}
