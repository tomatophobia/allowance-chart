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

  // TODO chimney 사용한 변환 https://scalalandio.github.io/chimney/transformers/customizing-transformers.html#transforming-coproducts
  def fromCurrency(currency: Currency): Nation =
    currency match {
      case Currency.USD => USA
      case Currency.KRW => KOR
      case Currency.JPY => JPN
      case Currency.CNY => CHN
      case _ => ??? // TOOD 임시로 만들기
    }

  override def values: IndexedSeq[Nation] = findValues

  implicit val eqNation: Eq[Nation] = Eq.fromUniversalEquals
}
