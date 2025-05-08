package com.syn2core.common.core.base

interface ErrorHandler {
    fun from(throwable: Throwable): ErrorEntity
}