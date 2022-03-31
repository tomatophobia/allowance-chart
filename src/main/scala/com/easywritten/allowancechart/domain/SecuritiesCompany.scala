package com.easywritten.allowancechart.domain

import cats.kernel.Eq
import enumeratum._

sealed trait SecuritiesCompany extends EnumEntry with Product with Serializable

object SecuritiesCompany extends Enum[SecuritiesCompany] {

  case object Daishin extends SecuritiesCompany
  case object Nonghyup extends SecuritiesCompany

  override def values: IndexedSeq[SecuritiesCompany] = findValues

  implicit val eqSecuritiesCompany: Eq[SecuritiesCompany] = Eq.fromUniversalEquals
}
