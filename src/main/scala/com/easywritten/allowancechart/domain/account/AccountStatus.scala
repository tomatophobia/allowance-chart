package com.easywritten.allowancechart.domain.account

sealed trait AccountStatus extends Product with Serializable

object AccountStatus {
  case object Uninitialized extends AccountStatus
  case object Normal extends AccountStatus
}
