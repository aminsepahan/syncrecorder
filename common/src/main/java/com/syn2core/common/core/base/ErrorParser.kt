package com.syn2core.common.core.base

interface ErrorParser {
    fun parse(errorEntity: ErrorEntity): String
}