package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.Money
import zio.entity.test.TestEntityRuntime.testEntityWithProbe
import zio.test._
import zio.test.Assertion._

object AccountRegisterSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("AccountRegisterSpec")(
      testM("All events are rejected before register account") {
        val key = AccountName("key")

        for {
          (accountEntity, _) <- testEntityWithProbe[
            AccountName,
            Account,
            AccountState,
            AccountEvent,
            AccountCommandReject
          ]

          failure <- accountEntity(key).deposit(Money.usd(32.15)).run
        } yield assert(failure)(fails(equalTo(AccountCommandReject.AccountNotInitialized)))
      }
    ).provideCustomLayer(TestAccountEntity.layer)
}
