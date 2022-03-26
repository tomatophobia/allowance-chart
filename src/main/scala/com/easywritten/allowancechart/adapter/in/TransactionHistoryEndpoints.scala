package com.easywritten.allowancechart.adapter.in

import cats.syntax.all._
import com.easywritten.allowancechart.adapter.in.page.RegisterTransactionHistoryPage
import com.easywritten.allowancechart.application.port.in.RegisterTransactionHistoryPort
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.account.AccountName
import io.circe.generic.auto._
import sttp.model.Part
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._

object TransactionHistoryEndpoints extends ErrorMapping {

  // TODO Error를 스트링 대신 다른 것으로
  val getRegisterPage: ZServerEndpoint[Env, Unit, ServiceError, String] =
    endpoint.get
      .in("transaction-history" / "register-page")
      .out(htmlBodyUtf8)
      .errorOut(customErrorBody())
      .tag(ApiDocTag.transactionHistory)
      .summary("Transaction history register page")
      .zServerLogic { _ =>
        ZIO.succeed(RegisterTransactionHistoryPage.html)
      }

  final case class NameWithTransactionHistory(name: String, securitiesCompany: String, transactionHistory: Part[TapirFile])

  @SuppressWarnings(Array("org.wartremover.warts.Serializable", "org.wartremover.warts.JavaSerializable"))
  val registerTransactionHistory: ZServerEndpoint[Env, NameWithTransactionHistory, ServiceError, Unit] =
    endpoint.post
      .in("transaction-history")
      .in(multipartBody[NameWithTransactionHistory])
      .errorOut(customErrorBody())
      .tag(ApiDocTag.transactionHistory)
      .summary("Register transaction history file")
      .zServerLogic { case NameWithTransactionHistory(name, company, transactionHistoryPart) =>
        RegisterTransactionHistoryPort.registerTransactionHistory(AccountName(name), Nil)
      }

  val all: List[ZServerEndpoint[Env, _, _, _]] =
    List(
      getRegisterPage,
      registerTransactionHistory
    )

  type Env = Has[RegisterTransactionHistoryPort]

}
