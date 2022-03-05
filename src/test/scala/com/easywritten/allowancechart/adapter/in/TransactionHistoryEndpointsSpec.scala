package com.easywritten.allowancechart.adapter.in

import sttp.capabilities.WebSockets
import sttp.client3._
import sttp.client3.impl.zio.RIOMonadAsyncError
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode.Ok
import sttp.tapir.server.stub.RichSttpBackendStub
import zio._
import zio.test._
import zio.test.Assertion._

object TransactionHistoryEndpointsSpec extends DefaultRunnableSpec {
  import TransactionHistoryEndpoints._

  override def spec: ZSpec[Environment, Failure] =
    suite("TransactionHistoryEndpointsSpec")(
      testM("register page returns status code 200 with html string") {
        val zioBackendStub = SttpBackendStub[Task, WebSockets](new RIOMonadAsyncError[Env])
        // RichSttpBackendStub이 정확히 어떤 기능을 더 추가해주는지는 모름
        val backendStub = RichSttpBackendStub(zioBackendStub).whenRequestMatchesEndpointThenLogic(registerPage)
        for {
          response <- basicRequest.get(uri"http://test.com/transaction-history/register").send(backendStub)
        } yield assert(response.code)(equalTo(Ok)) &&
          assert(response.body)(isRight(isNonEmptyString))
      }
    )
}
