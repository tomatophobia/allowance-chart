package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Money, MoneyBag}
import zio._
import zio.entity.annotations.Id

trait Account {
  @Id(1)
  def balance: IO[AccountCommandReject, MoneyBag]

  @Id(2)
  def deposit(money: Money): IO[AccountCommandReject, Unit]
}
