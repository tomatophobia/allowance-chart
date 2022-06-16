package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.domain.account.{
  Account,
  AccountCommandReject,
  AccountEvent,
  AccountName,
  AccountState
}
import zio.{Has, URLayer}
import zio.entity.core.Entity

// 도메인 계층에서 이벤트 소싱 구현에 너무 강하게 의존하고 있지 않나? => 근데 이벤트 소싱에서 바꿀 생각이 없는 걸 수도 있잖아...
final case class Asset(accounts: Entity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject])

object Asset {
  val layer: URLayer[Has[Entity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject]], Has[Asset]] =
    (Asset(_)).toLayer
}
