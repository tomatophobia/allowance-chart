package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, SecuritiesCompany, Stock}
import zio._
import zio.entity.annotations.Id

import java.time.Instant

trait Account {
  @Id(1)
  def initialize(company: SecuritiesCompany): IO[AccountError, Unit]

  @Id(2)
  def balance: IO[AccountError, MoneyBag]

  @Id(3)
  def holdings: IO[AccountError, Set[Holding]]

  @Id(4)
  def netValue: IO[AccountError, MoneyBag]

  @Id(5)
  def deposit(money: Money, at: Instant): IO[AccountError, Unit]

  @Id(6)
  def withdraw(money: Money, at: Instant): IO[AccountError, Unit]

  @Id(7)
  def buy(
      stock: Stock,
      unitPrice: Money,
      quantity: Int,
      at: Instant
  ): IO[AccountError, Unit]

  @Id(8)
  def sell(
      stock: Stock,
      contractPrice: Money,
      quantity: Int,
      at: Instant
  ): IO[AccountError, Unit]

  @Id(9)
  def dividendPaid(
      stock: Stock,
      amount: Money,
      tax: Money,
      at: Instant
  ): IO[AccountError, Unit]

  @Id(10)
  def foreignExchangeBuy(
      exchange: MoneyBag,
      exchangeRate: BigDecimal,
      at: Instant
  ): IO[AccountError, Unit]

}
