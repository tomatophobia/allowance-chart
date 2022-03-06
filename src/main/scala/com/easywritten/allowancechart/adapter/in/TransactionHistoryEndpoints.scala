package com.easywritten.allowancechart.adapter.in

import cats.syntax.all._
import com.easywritten.allowancechart.adapter.in.page.RegisterTransactionHistory
import io.circe.generic.auto._
import sttp.model.Part
import sttp.tapir.{CodecFormat, Endpoint, Schema}
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._

object TransactionHistoryEndpoints {

  // TODO Error를 스트링 대신 다른 것으로
  val getRegisterPage: ZServerEndpoint[Env, Unit, String, String] =
    endpoint.get
      .in("transaction-history" / "register-page")
      .out(htmlBodyUtf8)
      .errorOut(stringBody)
      .tag(ApiDocTag.transactionHistory)
      .summary("Transaction history register page")
      .zServerLogic { _ =>
        UIO.succeed(RegisterTransactionHistory.html)
      }

  final case class NameWithTransactionHistory(name: String, transactionHistory: Part[TapirFile])

  val registerTransactionHistory: ZServerEndpoint[Env, NameWithTransactionHistory, String, Unit] =
    endpoint.post
      .in("transaction-history")
      .in(multipartBody[NameWithTransactionHistory])
      .errorOut(stringBody)
      .tag(ApiDocTag.transactionHistory)
      .summary("Register transaction history file")
      .zServerLogic { case NameWithTransactionHistory(name, transactionHistoryPart) =>
        println(name)
        println(transactionHistoryPart.name)
        println(transactionHistoryPart.otherDispositionParams)
        println(transactionHistoryPart.headers)
        UIO.unit
      }

  final case class Pet(species: String, url: String)

  val petEndpoint: ZServerEndpoint[Env, Int, String, Pet] =
    endpoint.get
      .in("pet" / path[Int]("petId"))
      .errorOut(stringBody)
      .out(jsonBody[Pet])
      .zServerLogic { petId =>
        if (petId === 35) {
          UIO(Pet("Tapirus terrestris", "https://en.wikipedia.org/wiki/Tapir"))
        } else {
          IO.fail("Unknown pet id")
        }
      }

  val all: List[ZServerEndpoint[Env, _, _, _]] =
    List(
      getRegisterPage,
      registerTransactionHistory
    )

  type Env = Any

}
