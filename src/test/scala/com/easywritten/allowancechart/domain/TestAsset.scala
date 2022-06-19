package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.domain.account.{
  Account,
  AccountCommandReject,
  AccountEvent,
  AccountName,
  AccountState,
  TestAccountEntity
}
import zio._
import zio.clock.Clock
import zio.entity.core.Entity
import zio.entity.test.TestEntityRuntime.TestEntity
import zio.test.TestFailure

// TODO TestAsset은 Asset을 확장하는 구조여야 하지 않나? ...extends Asset? TestClock을 보고 다시 생각해보기
final case class TestAsset(accounts: TestEntity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject])

object TestAsset {

  val layer: ZLayer[Clock, TestFailure[Throwable], Has[Asset] with Has[TestAsset]] = TestAccountEntity.layer map { l =>
    val entity = l.get[Entity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject]]
    val testEntity = l.get[TestEntity[AccountName, Account, AccountState, AccountEvent, AccountCommandReject]]
    Has(Asset(entity)) ++ Has(TestAsset(testEntity))
  }
}
