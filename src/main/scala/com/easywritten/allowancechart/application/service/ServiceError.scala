package com.easywritten.allowancechart.application.service

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}

@SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.DefaultArguments"))
sealed abstract class ServiceError(val message: String, cause: Throwable = null)
    extends Throwable(message, cause)
    with Product
    with Serializable {
  val maybeCause: Option[Throwable] = Option(cause)
}

object ServiceError {
  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.DefaultArguments"))
  final case class InternalServerError(override val message: String, cause: Option[Throwable] = None)
      extends ServiceError(s"Internal Server Error: ${message}", cause.orNull)

  case object Unknown extends ServiceError("Unknown Error") // for [[ErrorMapping#customErrorBody]]

  // 이렇게 하면 ServiceError의 모든 하위 타입에 대해 인코더 인스턴스가 생성됨
  implicit def circeEncoderForServiceError[A <: ServiceError]: Encoder[A] = new Encoder[A] {
    override def apply(e: A): Json =
      Json.obj(
        "message" -> Json.fromString(e.message)
      )
  }

  implicit def circeDecoderForServiceError[A <: ServiceError]: Decoder[A] = new Decoder[A] {
    final def apply(c: HCursor): Decoder.Result[A] = Left(
      DecodingFailure("Decoding `ServiceError` using circe is not supported", Nil)
    )
    // TODO 모든 하위클래스별로 디코더 설정을 하든지 ServiceError를 case class로 바꾸든지
//      for {
//        message <- c.downField("message").as[String]
//      } yield (ServiceError(s"decoded from circe: ${message}")
  }
}
