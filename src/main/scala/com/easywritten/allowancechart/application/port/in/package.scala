package com.easywritten.allowancechart.application.port

import zio.Has

package object in {
  type RegisterTransactionHistory = Has[RegisterTransactionHistory.Service]
}
