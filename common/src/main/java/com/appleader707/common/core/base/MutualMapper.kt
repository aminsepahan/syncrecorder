package com.appleader707.common.core.base

interface MutualMapper<I, O> {
    fun from(from: I): O
    fun fromReverse(from: O): I
    fun from(from: List<I>): List<O> = from.map { from(it) }
    fun fromReverse(from: List<O>): List<I> = from.map { fromReverse(it) }
}