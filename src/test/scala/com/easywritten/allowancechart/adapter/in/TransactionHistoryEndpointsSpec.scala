package com.easywritten.allowancechart.adapter.in

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.impl.zio.RIOMonadAsyncError
import sttp.client3.internal.SttpFile
import sttp.client3.testing.SttpBackendStub
import sttp.model.{MediaType, Part}
import sttp.model.StatusCode.Ok
import sttp.tapir.server.stub.RichSttpBackendStub
import zio._
import zio.test._
import zio.test.Assertion._

import java.nio.file.Paths

object TransactionHistoryEndpointsSpec extends DefaultRunnableSpec {
  import TransactionHistoryEndpoints._

  override def spec: ZSpec[Environment, Failure] =
    suite("TransactionHistoryEndpointsSpec")(
      testM("register page returns status code 200 with html string") {
        val zioBackendStub = SttpBackendStub[Task, WebSockets with ZioStreams](new RIOMonadAsyncError[Env])
        // RichSttpBackendStub이 정확히 어떤 기능을 더 추가해주는지는 모름
        val backendStub = RichSttpBackendStub(zioBackendStub).whenRequestMatchesEndpointThenLogic(getRegisterPage)
        for {
          response <- basicRequest.get(uri"http://test.com/transaction-history/register").send(backendStub)
        } yield assert(response.code)(equalTo(Ok)) &&
          assert(response.body)(isRight(isNonEmptyString))
      },
      // 현재 tapir 버전 문제로 실행 불가
      // #42 https://github.com/tomatophobia/allowance-chart/pull/42#issuecomment-1060725204
      testM("post transaction-history file api returns status code 200") {
        val zioBackendStub = SttpBackendStub[Task, WebSockets with ZioStreams](new RIOMonadAsyncError[Env])
        // RichSttpBackendStub이 정확히 어떤 기능을 더 추가해주는지는 모름
        val backendStub =
          RichSttpBackendStub(zioBackendStub).whenRequestMatchesEndpointThenLogic(registerTransactionHistory)

        for {
          response <- basicRequest
            .post(uri"http://test.com/transaction-history")
            .multipartBody(
              List[Part[BasicRequestBody]](
                Part("name", StringBody("NH투자증권", "utf-8")),
                Part("transactionHistory", FileBody(SttpFile.fromPath(Paths.get("creon.csv")), MediaType.TextCsv))
              )
            )
            .send(backendStub)
        } yield assert(response.code)(equalTo(Ok)) &&
          assert(response.body)(isRight(isNonEmptyString))
      } @@ TestAspect.ignore
    )
}
