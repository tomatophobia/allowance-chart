package com.easywritten.allowancechart.application.service

sealed abstract class ServiceError(val errorCode: Int, val message: String)
    extends Throwable(message)
    with Product
    with Serializable

object ServiceError {}
