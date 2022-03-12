package com.easywritten.allowancechart.application.service

sealed abstract class ServiceError(val message: String) extends Throwable(message) with Product with Serializable

object ServiceError {
  case object InternalServerError extends ServiceError("Internal Server Error")
  case object Unknown extends ServiceError("Unknown Error") // for [[ErrorMapping#customErrorBody]]
}
