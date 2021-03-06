package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.domain.account.{
  Account,
  AccountError,
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
final case class TestAsset(accounts: TestEntity[AccountName, Account, AccountState, AccountEvent, AccountError])

object TestAsset {

  // TODO 제대로 다시 만들기
//  val layer: ZLayer[Clock, TestFailure[Throwable], Has[AssetLive] with Has[TestAsset]] = TestAccountEntity.layer map { l =>
//    val entity = l.get[Entity[AccountName, Account, AccountState, AccountEvent, AccountError]]
//    val testEntity = l.get[TestEntity[AccountName, Account, AccountState, AccountEvent, AccountError]]
//    Has(AssetLive(entity)) ++ Has(TestAsset(testEntity))
//  }
}
