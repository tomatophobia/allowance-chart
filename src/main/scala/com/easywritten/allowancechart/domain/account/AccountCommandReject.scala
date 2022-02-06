package com.easywritten.allowancechart.domain.account

import boopickle.Pickler
import boopickle.Default.generatePickler
import boopickle.Default.exceptionPickler
import boopickle.Default.stringPickler

@SuppressWarnings(Array("org.wartremover.warts.Null"))
sealed abstract class AccountCommandReject(message: String, cause: Option[Throwable])
    extends Throwable(message, cause.orNull)

object AccountCommandReject {

  final case class InsufficientBalance(message: String) extends AccountCommandReject(message, None)

  final case class InsufficientShares(message: String) extends AccountCommandReject(message, None)

  final case class FromThrowable(cause: Throwable) extends AccountCommandReject("Made from Throwable", Option(cause))

  case object Unknown extends AccountCommandReject("Account command rejected for unknown reason", None)

  implicit val throwablePickler: Pickler[Throwable] = exceptionPickler

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  implicit val optionThrowablePickler: Pickler[Option[Throwable]] = exceptionPickler.xmap(Option.apply)(_.orNull)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  implicit val accountCommandRejectPickler: Pickler[AccountCommandReject] = generatePickler

}
