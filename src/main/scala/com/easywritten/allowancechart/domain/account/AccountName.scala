package com.easywritten.allowancechart.domain.account

import sttp.tapir.Schema
import sttp.tapir.generic.auto._

final case class AccountName(name: String) extends AnyVal

object AccountName {
  implicit lazy val tapirSchemaForAccountName: Schema[AccountName] = Schema.derived
}
