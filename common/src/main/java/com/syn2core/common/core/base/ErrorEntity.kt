package com.syn2core.common.core.base

sealed class ErrorEntity {
    data object Network : ErrorEntity()
    data object NotFound : ErrorEntity()
    data object AccessDenied : ErrorEntity()
    data object DontHaveUpdated : ErrorEntity()
    data object ServiceUnavailable : ErrorEntity()
    data object InternetConnection : ErrorEntity()
    data object ServerInternal : ErrorEntity()
    data object ProcessClosing : ErrorEntity()
    data class Unknown(val message: String? = null) : ErrorEntity()
}