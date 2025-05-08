package com.syn2core.syn2corecamera.presentation.ui.recording

import android.content.Context
import android.view.Surface
import com.syn2core.common.ui.base.BaseViewEvent
import com.syn2core.syn2corecamera.domain.RecordingSettings

/**
 *
 * @see BaseViewEvent
 */

sealed class RecordingViewEvent : BaseViewEvent {
    data class ToggleRecording(
        val context: Context,
        val cameraSurface: Surface
    ) : RecordingViewEvent()

    data class SaveSettings(val settings: RecordingSettings) : RecordingViewEvent()
    object ShowSettings : RecordingViewEvent()
    object HideSettings : RecordingViewEvent()
    object LoadSettings : RecordingViewEvent()
    object NavigateToShowByChart : RecordingViewEvent()
}