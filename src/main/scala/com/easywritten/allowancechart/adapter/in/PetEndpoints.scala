package com.easywritten.allowancechart.adapter.in

import cats.syntax.all._
import io.circe.generic.auto._
import sttp.tapir.Endpoint
import sttp.tapir.EndpointIO.Header
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._

object PetEndpoints {
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

  val htmlExampleEndpoint: ZServerEndpoint[Env, Int, String, String] =
    endpoint
      .in("example" / path[Int]("hello"))
      .errorOut(stringBody)
      .out(htmlBodyUtf8)
      .zServerLogic { x =>
        UIO.succeed(ExamplePage())
      }

  val all: List[ZServerEndpoint[Env, _, _, _]] =
    List(
      petEndpoint,
      htmlExampleEndpoint
    )

  type Env = Any

}
