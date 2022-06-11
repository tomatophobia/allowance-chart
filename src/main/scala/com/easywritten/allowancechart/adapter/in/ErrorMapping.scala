package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.service.ServiceError
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.EndpointOutput.OneOfMapping

trait ErrorMapping {

  // https://github.com/softwaremill/tapir/issues/129
  implicit val schemaForThrowable: Schema[Throwable] = Schema.string[Throwable]
  val schemaForInternalServerError: Schema[ServiceError.InternalServerError] = Schema.derived[ServiceError.InternalServerError]
  val schemaForUnknown: Schema[ServiceError.Unknown.type] = Schema.derived[ServiceError.Unknown.type]
  implicit val scehmaForServiceError: Schema[ServiceError] =
    Schema.oneOfUsingField[ServiceError, String](_.getClass.getSimpleName, _.toString)(
      "InternalServerError" -> schemaForInternalServerError,
      "Unknown" -> schemaForUnknown
    )

  def customErrorBody(
      statusCodeMapping: OneOfMapping[_ <: ServiceError]*
  ): EndpointOutput.OneOf[ServiceError, ServiceError] = {

    val defaults: Seq[OneOfMapping[_ <: ServiceError]] = Seq(
      oneOfMapping(
        StatusCode.InternalServerError,
        jsonBody[ServiceError.InternalServerError]
      )
    )

    oneOf[ServiceError](
      oneOfDefaultMapping(jsonBody[ServiceError.Unknown.type]),
      defaults ++ statusCodeMapping: _*
    )
  }
}
