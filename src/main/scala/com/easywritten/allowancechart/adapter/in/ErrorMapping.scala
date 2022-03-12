package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.service.ServiceError
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.model.StatusCode
import io.circe.generic.auto._
import sttp.tapir.EndpointOutput.OneOfMapping

trait ErrorMapping {
  def customErrorBody(
      statusCodeMapping: OneOfMapping[_ <: ServiceError]*
  ): EndpointOutput.OneOf[ServiceError, ServiceError] = {

    val defaults: Seq[OneOfMapping[_ <: ServiceError]] = Seq(
      oneOfMapping(
        StatusCode.InternalServerError,
        jsonBody[ServiceError.InternalServerError.type]
      )
    )

    oneOf[ServiceError](
      oneOfDefaultMapping(jsonBody[ServiceError.Unknown.type]),
      defaults ++ statusCodeMapping: _*
    )
  }
}
