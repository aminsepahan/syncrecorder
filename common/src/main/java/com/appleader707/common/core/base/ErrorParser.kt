package com.appleader707.common.core.base

interface ErrorParser {
    fun parse(errorEntity: ErrorEntity): String
}