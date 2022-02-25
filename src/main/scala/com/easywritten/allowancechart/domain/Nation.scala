package com.easywritten.allowancechart.domain

import cats.kernel.Eq
import enumeratum._

sealed trait Nation extends EnumEntry with Product with Serializable

object Nation extends Enum[Nation] {
  // using ISO3 nation code
  case object USA extends Nation
  case object KOR extends Nation
  case object JPN extends Nation
  case object CHN extends Nation

  override def values: IndexedSeq[Nation] = findValues

  implicit val eqNation: Eq[Nation] = Eq.fromUniversalEquals
}
