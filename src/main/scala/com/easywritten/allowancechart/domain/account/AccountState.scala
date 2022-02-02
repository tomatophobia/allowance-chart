package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.MoneyBag
import zio.entity.core.Fold

final case class AccountState(
    balance: MoneyBag
)

object AccountState {
  val init: AccountState = AccountState(balance = MoneyBag.empty)
}
