package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import boopickle.Default.generatePickler
import boopickle.Default.exceptionPickler

@SuppressWarnings(Array("org.wartremover.warts.Null"))
sealed abstract class AccountCommandReject(message: String, cause: Option[Throwable])
    extends Throwable(message, cause.orNull)

object AccountCommandReject {

  final case class FromThrowable(cause: Option[Throwable]) extends AccountCommandReject("Made from Throwable", cause)

  final case object Unknown extends AccountCommandReject("Account command rejected for unknown reason", None)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountCommandRejectPickler: Pickler[AccountCommandReject] = generatePickler

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  implicit val optionThrowablePickler: Pickler[Option[Throwable]] = exceptionPickler.xmap(Option.apply)(_.orNull)

}
