package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import android.view.Surface
import com.appleader707.common.ui.base.BaseViewEvent
import com.appleader707.syncrecorder.domain.RecordingSettings

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
}