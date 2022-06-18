package com.easywritten.allowancechart.adapter.in

import cats.syntax.all._
import com.easywritten.allowancechart.adapter.in.page.RegisterTransactionRecordPage
import com.easywritten.allowancechart.application.port.in.{RegisterTransactionRecordPort, TransactionRecord}
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.SecuritiesCompany
import com.easywritten.allowancechart.domain.account.AccountName
import io.circe._
import io.circe.generic.auto._
import sttp.model.Part
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._

object TransactionRecordEndpoints extends ErrorMapping {

  // TODO Error를 스트링 대신 다른 것으로
  val getRegisterPage: ZServerEndpoint[Env, Unit, ServiceError, String] =
    endpoint.get
      .in("transaction-record" / "register-page")
      .out(htmlBodyUtf8)
      .errorOut(customErrorBody())
      .tag(ApiDocTag.transactionRecord)
      .summary("Transaction record register page")
      .zServerLogic { _ =>
        ZIO.succeed(RegisterTransactionRecordPage.html)
      }

  final case class NameWithTransactionRecord(
      accountName: String,
      securitiesCompany: String,
      transactionRecord: Part[java.io.File]
  )

  @SuppressWarnings(Array("org.wartremover.warts.Serializable", "org.wartremover.warts.JavaSerializable"))
  val registerTransactionRecord: ZServerEndpoint[Env, NameWithTransactionRecord, ServiceError, Unit] =
    endpoint.post
      .in("transaction-record")
      .in(multipartBody[NameWithTransactionRecord])
      .errorOut(customErrorBody())
      .tag(ApiDocTag.transactionRecord)
      .summary("Register transaction record file")
      .zServerLogic { case NameWithTransactionRecord(name, company, transactionRecordPart) =>
        val securitiesCompany = SecuritiesCompany.withName(company)

        for {
          // TODO 증권사 이름 parsing 도중 에러 처리
          records <- TransactionRecordParser.fromFile(transactionRecordPart.body, securitiesCompany)

          _ <- RegisterTransactionRecordPort.registerTransactionRecord(
            AccountName(name),
            securitiesCompany,
            records
          )
        } yield ()
      }

  val all: List[ZServerEndpoint[Env, _, _, _]] =
    List(
      getRegisterPage,
      registerTransactionRecord
    )

  type Env = Has[RegisterTransactionRecordPort]

}
