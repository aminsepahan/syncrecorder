package com.syn2core.syn2corecamera.presentation.ui.recording

import android.content.Context
import android.view.Surface
import com.syn2core.common.ui.base.BaseViewEvent

/**
 *
 * @see BaseViewEvent
 */

sealed class RecordingViewEvent : BaseViewEvent {
    data class ToggleRecording(
        val context: Context,
        val cameraSurface: Surface
    ) : RecordingViewEvent()
    object NavigateToSettings : RecordingViewEvent()
    object LoadSettings : RecordingViewEvent()
}