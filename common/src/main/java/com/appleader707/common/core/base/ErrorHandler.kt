package com.appleader707.common.core.base

interface ErrorHandler {
    fun from(throwable: Throwable): ErrorEntity
}