package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import boopickle.Default.generatePickler
import boopickle.Default.exceptionPickler

sealed abstract class AccountCommandReject(message: String, cause: Option[Throwable] = None)
    extends Throwable(message, cause.orNull)

object AccountCommandReject {

  final case class FromThrowable(cause: Option[Throwable]) extends AccountCommandReject("Made from Throwable", cause)

  final case object Unknown extends AccountCommandReject("Account command rejected for unknown reason")

  implicit val accountCommandRejectPickler: Pickler[AccountCommandReject] = generatePickler

  implicit val optionThrowablePickler: Pickler[Option[Throwable]] = exceptionPickler.xmap(Option.apply)(_.orNull)

}
