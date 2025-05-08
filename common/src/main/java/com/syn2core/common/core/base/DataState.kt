package com.syn2core.common.core.base

sealed class DataState<out R> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class Failure(val errorEntity: ErrorEntity) : DataState<Nothing>()
}