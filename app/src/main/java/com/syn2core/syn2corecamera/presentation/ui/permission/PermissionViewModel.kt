package com.syn2core.syn2corecamera.presentation.ui.permission

import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class PermissionViewModel @Inject constructor(
) : BaseViewModel<PermissionViewEvent>() {

    private val _state = MutableStateFlow(PermissionViewState())
    val state: StateFlow<PermissionViewState> = _state.asStateFlow()

    val effect = SingleLiveData<PermissionViewEffect>()

    override fun processEvent(event: PermissionViewEvent) {
        when (event) {
            PermissionViewEvent.GoRecordingPage -> {
                effect.postValue(PermissionViewEffect.GoRecordingPage)
            }
        }
    }

    private fun updateState(update: (PermissionViewState) -> PermissionViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(PermissionViewEffect.DoNothing)
    }
}