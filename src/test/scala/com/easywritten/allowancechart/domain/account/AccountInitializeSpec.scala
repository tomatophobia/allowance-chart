package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.Money
import zio.ZIO
import zio.clock.Clock
import zio.entity.test.TestEntityRuntime.testEntityWithProbe
import zio.test._
import zio.test.Assertion._

object AccountInitializeSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("AccountInitializeSpec")(
      testM("All events are rejected before initialize account") {
        val key = AccountName("key")

        for {
          now <- ZIO.accessM[Clock](_.get.currentDateTime.map(_.toInstant))
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]
          account = accountEntity(key)
          failure <- account.deposit(Money.usd(32.15), now).run
        } yield assert(failure)(fails(equalTo(AccountCommandReject.AccountNotInitialized)))
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
