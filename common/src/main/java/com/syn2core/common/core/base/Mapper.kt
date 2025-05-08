package com.syn2core.common.core.base

interface Mapper<I, O> {
    fun from(from: I): O
}
