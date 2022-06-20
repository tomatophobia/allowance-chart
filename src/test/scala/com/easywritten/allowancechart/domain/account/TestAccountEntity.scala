package com.easywritten.allowancechart.domain.account

import zio.{Has, ZLayer}
import zio.clock.Clock
import zio.duration.durationInt
import zio.entity.core.{Entity, EventSourcedBehaviour}
import zio.entity.test.TestEntityRuntime.testEntity
import zio.entity.test.{TestEntityRuntime, TestMemoryStores}
import zio.test.TestFailure

object TestAccountEntity {
  import AccountCommandHandler.accountProtocol

  val layer: ZLayer[Clock, TestFailure[Throwable], Has[
    Entity[AccountName, Account, AccountState, AccountEvent, AccountError]
  ] with Has[TestEntityRuntime.TestEntity[AccountName, Account, AccountState, AccountEvent, AccountError]]] =
    (Clock.any and TestMemoryStores.make[AccountName, AccountEvent, AccountState](50.millis) to
      testEntity(
        AccountCommandHandler.tagging,
        EventSourcedBehaviour[Account, AccountState, AccountEvent, AccountError](
          new AccountCommandHandler(_),
          AccountCommandHandler.eventHandlerLogic,
          AccountError.FromThrowable
        )
      )).mapError(TestFailure.fail)
}
