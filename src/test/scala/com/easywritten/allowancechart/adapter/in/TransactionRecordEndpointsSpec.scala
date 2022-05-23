package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.service.RegisterTransactionRecordService
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

object TransactionRecordEndpointsSpec extends DefaultRunnableSpec {
  import TransactionRecordEndpoints._

  override def spec: ZSpec[Environment, Failure] =
    suite("TransactionRecordEndpointsSpec")(
      testM("register page returns status code 200 with html string") {
        val zioBackendStub = SttpBackendStub[RIO[Env, *], WebSockets with ZioStreams](new RIOMonadAsyncError[Env])
        // RichSttpBackendStub이 정확히 어떤 기능을 더 추가해주는지는 모름
        val backendStub = RichSttpBackendStub(zioBackendStub).whenRequestMatchesEndpointThenLogic(getRegisterPage)
        for {
          response <- basicRequest.get(uri"http://test.com/transaction-record/register-page").send(backendStub)
        } yield assert(response.code)(equalTo(Ok)) &&
          assert(response.body)(isRight(isNonEmptyString))
      },
      // TODO 현재 tapir 버전 문제로 실행 불가
      // #42 https://github.com/tomatophobia/allowance-chart/pull/42#issuecomment-1060725204
      testM("post transaction-record file api returns status code 200") {
        val zioBackendStub = SttpBackendStub[RIO[Env, *], WebSockets with ZioStreams](new RIOMonadAsyncError[Env])
        // RichSttpBackendStub이 정확히 어떤 기능을 더 추가해주는지는 모름
        val backendStub =
          RichSttpBackendStub(zioBackendStub).whenRequestMatchesEndpointThenLogic(registerTransactionRecord)

        for {
          response <- basicRequest
            .post(uri"http://test.com/transaction-record")
            .multipartBody(
              List[Part[BasicRequestBody]](
                Part("name", StringBody("NH투자증권", "utf-8")),
                Part("transactionRecord", FileBody(SttpFile.fromPath(Paths.get("namuh.csv")), MediaType.TextCsv))
              )
            )
            .send(backendStub)
        } yield assert(response.code)(equalTo(Ok)) &&
          assert(response.body)(isRight(isNonEmptyString))
      } @@ TestAspect.ignore
    ).provideCustomLayer(RegisterTransactionRecordService.layer)
}