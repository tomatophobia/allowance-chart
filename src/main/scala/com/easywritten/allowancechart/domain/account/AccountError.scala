package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import boopickle.Default.generatePickler
import boopickle.Default.exceptionPickler
import boopickle.Default.stringPickler

@SuppressWarnings(Array("org.wartremover.warts.Null"))
sealed abstract class AccountError(message: String, cause: Option[Throwable]) extends Throwable(message, cause.orNull)

object AccountError {

  case object AccountNotInitialized extends AccountError("Account is not initialized", None)

  case object AccountAlreadyInitialized extends AccountError("Account is already initialized", None)

  final case class InsufficientBalance(message: String) extends AccountError(message, None)

  final case class InsufficientShares(message: String) extends AccountError(message, None)

  final case class FromThrowable(cause: Throwable) extends AccountError("Made from Throwable", Option(cause))

  case object Unknown extends AccountError("Account command rejected for unknown reason", None)

  implicit val throwablePickler: Pickler[Throwable] = exceptionPickler

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  implicit val optionThrowablePickler: Pickler[Option[Throwable]] = exceptionPickler.xmap(Option.apply)(_.orNull)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountErrorPickler: Pickler[AccountError] = generatePickler

}
