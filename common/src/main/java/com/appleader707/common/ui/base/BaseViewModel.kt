package com.appleader707.common.ui.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<E : BaseViewEvent> : ViewModel() {
    abstract fun processEvent(event: E)
}