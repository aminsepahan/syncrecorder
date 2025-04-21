package com.appleader707.common.core.base

interface Mapper<I, O> {
    fun from(from: I): O
}
